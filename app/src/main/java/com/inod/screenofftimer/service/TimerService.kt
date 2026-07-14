package com.inod.screenofftimer.service

//noinspection SuspiciousImport
import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.content.edit
import com.inod.screenofftimer.core.notification.Notifications
import com.inod.screenofftimer.utils.MediaUtils
import com.inod.screenofftimer.utils.lockScreenDevice
import com.inod.screenofftimer.data.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class TimerService() : Service() {

    companion object {
        var isServiceRunning = false
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_UPDATE = "ACTION_UPDATE"
        const val ACTION_START = "ACTION_START"
        const val ACTION_ADD_MINUTE = "ACTION_ADD_MINUTE"
        const val ACTION_REDUCE_MINUTE = "ACTION_REDUCE_MINUTE"
    }

    private val notification = Notifications()

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
        notification.defaultNotification(this, 0)
        startForeground(1, notification.defaultNotification(this, 0))
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private fun startForegroundCompat(remaining: Int) {
        try {
            val notif = notification.defaultNotification(this, remaining)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(1, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(1, notif)
            }
        } catch (e: Exception) {
            Log.w("TIMER_SVC", "startForegroundCompat failed: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            null -> {
                val endTime = getEndTime()
                val remaining = ((endTime - System.currentTimeMillis()) / 1000).toInt()

                val hasNotifPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                            android.content.pm.PackageManager.PERMISSION_GRANTED
                } else true

                if (remaining > 0 && hasNotifPermission) {
                    isServiceRunning = true
                    Prefs.saveRunning(applicationContext, true)
                    startForegroundCompat(remaining)
                    startTimer()
                } else {
                    saveEndTime(0)
                    saveRemainingTime(0)
                    Prefs.saveRunning(applicationContext, false)
                    stopSelf()
                }
                return START_STICKY
            }

            ACTION_START -> {
                val duration = intent.getIntExtra("time", -1)

                if (duration <= 0) {
                    stopSelf()
                    Prefs.saveRunning(applicationContext, false)
                    return START_NOT_STICKY
                }

                saveRemainingTime(0)
                Prefs.saveLeftSeconds(applicationContext, duration)
                val endTime = System.currentTimeMillis() + (duration * 1000L)
                saveEndTime(endTime)

                startForegroundCompat(duration)
                startTimer()
                return START_STICKY
            }

            ACTION_ADD_MINUTE -> {
                val extra = intent.getIntExtra("time_update", 10 * 60)
                val currentEndTime = getEndTime()
                val newEndTime = currentEndTime + (extra * 1000L)

                saveEndTime(newEndTime)

                val remaining = ((newEndTime - System.currentTimeMillis()) / 1000).toInt()

                saveRemainingTime(remaining)
                Prefs.saveLeftSeconds(applicationContext, remaining)
                startForegroundCompat(remaining)
                return START_STICKY
            }

            ACTION_REDUCE_MINUTE -> {
                val extra = intent.getIntExtra("time_update", 10 * 60)
                val currentEndTime = getEndTime()
                val newEndTime = currentEndTime - (extra * 1000L)
                saveEndTime(newEndTime)

                val remaining = ((newEndTime - System.currentTimeMillis()) / 1000).toInt()
                saveRemainingTime(remaining)
                Prefs.saveLeftSeconds(applicationContext, remaining)

                startForegroundCompat(remaining)

                return START_STICKY
            }

            ACTION_UPDATE -> {
                scope.coroutineContext.cancelChildren()

                val newDuration = intent.getIntExtra("time", 0)
                val newEndTime = System.currentTimeMillis() + (newDuration * 1000L)

                saveEndTime(newEndTime)
                saveRemainingTime(newDuration)

                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()

                return START_NOT_STICKY
            }

            ACTION_STOP -> {
                scope.coroutineContext.cancelChildren()

                saveEndTime(0)          // ← reset total, bukan hitung ulang ke masa depan
                saveRemainingTime(0)
                Prefs.saveRunning(applicationContext, false)

                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()

                return START_NOT_STICKY
            }
        }
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun startTimer() {
        scope.coroutineContext.cancelChildren()

        scope.launch {
            while (isActive) {
                val remainingMillis = getEndTime() - System.currentTimeMillis()
                val remaining = (remainingMillis / 1000).toInt()

                if (remaining <= 0) break

                try {
                    updateNotification(remaining)
                } catch (e: Exception) {
                    Log.w("TIMER_SVC", "updateNotification failed: ${e.message}")
                }

                Prefs.saveLeftSeconds(applicationContext, remaining)
                sendUpdate(remaining)

                delay(1000.milliseconds)
            }

            val lastDrag = Prefs.getLastDrag(application)
            try {
                updateNotification(0)
            } catch (e: Exception) {
                Log.w("TIMER_SVC", "updateNotification failed: ${e.message}")
            }
            sendUpdate(lastDrag)
            Prefs.saveRunning(applicationContext, false)

            saveEndTime(0)
            saveRemainingTime(0)
            onTimerFinished()
        }
    }

    private fun saveRemainingTime(time: Int) {
        getSharedPreferences("timer", MODE_PRIVATE)
            .edit {
                putInt("remaining_time", time)
            }
    }

    private fun getRemainingTime(): Int {
        return getSharedPreferences("timer", MODE_PRIVATE)
            .getInt("remaining_time", 0)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun updateNotification(time: Int) {
        notification.defaultNotificationCompat(this, time)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        isServiceRunning = false
        Prefs.saveRunning(applicationContext, false)
        scope.cancel()
        super.onDestroy()
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        super.onTimeout(startId, fgsType)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf(startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        val remaining = ((getEndTime() - System.currentTimeMillis()) / 1000).toInt()
        if (remaining <= 0) {
            scope.launch {
                Prefs.saveRunning(applicationContext, false)
                delay(50.milliseconds)
                stopSelf()
            }
        }
    }

    private fun saveEndTime(time: Long) {
        getSharedPreferences("timer", MODE_PRIVATE)
            .edit {
                putLong("end_time", time)
            }
    }

    private fun getEndTime(): Long {
        return getSharedPreferences("timer", MODE_PRIVATE)
            .getLong("end_time", 0)
    }

    private fun sendUpdate(seconds: Int) {
//        Log.d("TIMER_SVC", "SEND: $seconds")
        val intent = Intent("TIMER_TICK").apply {
            setPackage(packageName)
            putExtra("time_left", seconds)
        }
        sendBroadcast(intent)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private suspend fun onTimerFinished() {
        if (Prefs.isGoHome(applicationContext)) {
            goHome(applicationContext)
        }
        if (Prefs.isStopMedia(applicationContext)) {
            stopMedia(applicationContext)
        }

        delay(500.milliseconds)
        if (Prefs.isLockScreen(applicationContext)) {
            lockScreen(applicationContext)
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun lockScreen(context: Context) {
        lockScreenDevice(context)
    }

    fun stopMedia(context: Context) {
        MediaUtils().stopMedia(applicationContext)
    }

    fun goHome(context: Context) {
        MediaUtils().goToHome(applicationContext)
    }
}