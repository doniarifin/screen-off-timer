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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.content.edit
import com.inod.screenofftimer.data.prefs.Prefs
import com.inod.screenofftimer.core.notification.Notifications
import com.inod.screenofftimer.core.utils.MediaUtils
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.isActive

class TimerService() : Service() {

    companion object {
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
        notification.defaultNotification(this, 0)
        startForeground(1, notification.defaultNotification(this, 0))
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {

            ACTION_START -> {
                val duration = intent.getIntExtra("time", -1) ?: -1
                val savedRemaining = getRemainingTime()

                val finalDuration = when {
                    savedRemaining > 0 -> savedRemaining
                    duration > 0 -> duration
                    else -> 0
                }

                if (finalDuration <= 0) {
                    stopSelf()
                    Prefs.saveRunning(applicationContext, false)
                    return START_NOT_STICKY
                }

                Prefs.saveLeftSeconds(applicationContext, finalDuration)

                val endTime = System.currentTimeMillis() + (finalDuration * 1000L)
                saveEndTime(endTime)
                saveRemainingTime(0)

                startForeground(
                    1,
                    notification.defaultNotification(this, finalDuration),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )

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

                startForeground(
                    1,
                    notification.defaultNotification(this, remaining),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )

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

                startForeground(
                    1,
                    notification.defaultNotification(this, remaining),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )

                return START_STICKY
            }

            ACTION_UPDATE -> {
                val newDuration = intent.getIntExtra("time", 0)
                val newEndTime = System.currentTimeMillis() + (newDuration * 1000L)

                Log.d("UPDATE", "SECONDS: $newDuration")

                saveEndTime(newEndTime)
                saveRemainingTime(newDuration)

                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()

                return START_NOT_STICKY
            }

            ACTION_STOP -> {
                val newDuration = intent.getIntExtra("time", 0)
                val newEndTime = System.currentTimeMillis() + (newDuration * 1000L)
                saveEndTime(newEndTime)

                val remaining = ((getEndTime() - System.currentTimeMillis()) / 1000).toInt()
                saveRemainingTime(remaining)

                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()

                return START_NOT_STICKY
            }


        }

        return START_STICKY
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    private fun startTimer() {
        scope.coroutineContext.cancelChildren()

        scope.launch {
            while (isActive) {

                val remainingMillis = getEndTime() - System.currentTimeMillis()
                val remaining = (remainingMillis / 1000).toInt()

                if (remaining <= 0) break

                updateNotification(remaining)
                sendUpdate(remaining)

                delay(1000)
            }

            updateNotification(0)

            sendUpdate(0)

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
        Prefs.saveLeftSeconds(applicationContext, time)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d("IS_RUNNING", "STOP DI SINI")

        scope.launch {
            Prefs.saveRunning(applicationContext, false)
            delay(50)
            super.onDestroy()
        }
        scope.cancel()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        val remaining = ((getEndTime() - System.currentTimeMillis()) / 1000).toInt()
        val accessibility = Prefs.isAccessibility(applicationContext)

        if (remaining > 0) {
            if (!accessibility) Prefs.saveRunning(applicationContext, false)
        } else {
            scope.launch {
                Prefs.saveRunning(applicationContext, false)
                delay(50)
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
        Log.d("TIMER_SVC", "SEND: $seconds")
        val intent = Intent("TIMER_TICK").apply {
            setPackage(packageName)
            putExtra("time_left", seconds)
        }
        sendBroadcast(intent)
    }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    private fun onTimerFinished() {

        Prefs.saveRunning(applicationContext, false)
        Prefs.saveLeftSeconds(applicationContext, 0)

        if (Prefs.isStopMedia(applicationContext)) {
            stopMedia(applicationContext)
        }
        if (Prefs.isLockScreen(applicationContext)) {
            lockScreen(applicationContext)
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun lockScreen(context: Context) {
        val service = MediaControlAccessibilityService.instance

        if (service != null) {
            service.lockScreenNow()
        } else {
            Log.d("Media", "Accessibility not active")
        }
    }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    fun stopMedia(context: Context) {
        MediaUtils().stopMedia(applicationContext)
    }
}