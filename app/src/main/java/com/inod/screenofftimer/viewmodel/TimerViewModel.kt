package com.inod.screenofftimer.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.inod.screenofftimer.data.prefs.Prefs
import com.inod.screenofftimer.service.TimerService
import com.inod.screenofftimer.ui.enums.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class TimerViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()

    var accessibility by mutableStateOf(Prefs.isAccessibility(context))
        private set
    var theme by mutableStateOf(Prefs.getTheme(context))
        private set

//    private val _theme = MutableStateFlow(ThemeMode.SYSTEM)
//    val theme = _theme

    var isLockScreen by mutableStateOf(Prefs.isLockScreen(context))
        private set

    var isStopMedia by mutableStateOf(Prefs.isStopMedia(context))
        private set

    var leftSeconds by mutableIntStateOf(Prefs.getLeftSeconds(context))
        private set

    var minutes by mutableIntStateOf(Prefs.getMinutes(context))
        private set

    var isRunning by mutableStateOf(Prefs.isRunning(context))
        private set

    val getRunning = Prefs.runningFlow(context)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )

    fun updateAccessibility(value: Boolean) {
        accessibility = value
        Prefs.saveAccessibility(context, value)
        //update lockscreen
        updateLockScreen(value)
    }

    fun updateTheme(value: ThemeMode) {
        theme = value
        Prefs.saveTheme(context, value)
    }

    fun updateLockScreen(value: Boolean) {
        isLockScreen = value
        Prefs.saveLockScreen(context, value)
    }

    fun updateStopMedia(value: Boolean) {
        isStopMedia = value
        Prefs.saveStopMedia(context, value)
    }

    fun updateLeftSeconds(value: Int) {
        leftSeconds = value
        Prefs.saveLeftSeconds(context, value)
    }

    fun updateMinutes(value: Int) {
        minutes = value
        Prefs.saveMinutes(context, value)
    }

    fun updateRunning(value: Boolean) {
        isRunning = value
        Prefs.saveRunning(context, value)
    }

    init {
//        viewModelScope.launch {
//            dataStoreManager.getSettings(context).collect { settings ->
////                totalMinutes = settings.minutes
//            }
//            isRunning = Prefs.isRunning(context)
//            Log.d("GET", "GET RUNNING: $isRunning")
//        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent?.action == "TIMER_TICK") {
                val seconds = intent.getIntExtra("time_left", 0)
                leftSeconds = seconds

                if (seconds <= 0) isRunning = false
//                Log.d("TIMER_VM", "Received: $seconds")
            }
        }
    }

    private var isRegistered = false

    fun registerReceiver(context: Context) {
        Log.d("TIMER_VM", "REGISTER RECEIVER")

        if (isRegistered) return

        val appContext = context.applicationContext

        val filter = IntentFilter("TIMER_TICK")

        ContextCompat.registerReceiver(
            appContext,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        isRegistered = true
    }

    fun unregisterReceiver(context: Context) {
        if (!isRegistered) return

        try {
            context.applicationContext.unregisterReceiver(receiver)
        } catch (e: Exception) {
            Log.d("TIMER_VM", "safe ignore unregister")
        }

        isRegistered = false
    }

    fun startTimer(totalSeconds: Int) {
        val intent = Intent(application, TimerService::class.java).apply {
            putExtra("time", totalSeconds)
            action = TimerService.ACTION_START
        }

        Log.d("START", "start seconds: $totalSeconds")

        ContextCompat.startForegroundService(context, intent)
        updateRunning(true)
    }

    fun setTimer(min: Int) {
        leftSeconds = min * 60
        minutes = min

        updateMinutes(min)
        updateLeftSeconds(min * 60)

        updateTimer(application, min)
    }

    fun updateTimer(context: Context, minutes: Int) {
        val intent = Intent(context, TimerService::class.java).apply {
            putExtra("time", minutes * 60)
            action = TimerService.ACTION_UPDATE
        }

        updateRunning(false)
        context.startService(intent)
    }

    fun stop(seconds: Int) {
        updateRunning(false)
        stopTimer(application, seconds)
    }

    fun stopTimer(context: Context, seconds: Int) {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_STOP
            putExtra("time", leftSeconds)
        }

        Log.d("STOP", "stop at seconds: $leftSeconds")

        context.startService(intent)
    }
}
