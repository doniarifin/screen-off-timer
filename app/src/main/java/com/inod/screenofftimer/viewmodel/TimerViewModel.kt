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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.inod.screenofftimer.data.prefs.Prefs
import com.inod.screenofftimer.service.TimerService
import com.inod.screenofftimer.ui.enums.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TimerViewModel(
    application: Application,
    private val state: SavedStateHandle
) : AndroidViewModel(application) {
    private val context = getApplication<Application>()

    val allSettings = Prefs.settingsFlow(context)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = Prefs.AllSettings()
        )

    var theme by mutableStateOf(state.get<ThemeMode>("theme") ?: ThemeMode.SYSTEM)
        private set
    var isRunning by mutableStateOf(state.get<Boolean>("is_running") ?: false)
        private set
    var leftSeconds by mutableIntStateOf(state.get<Int>("left_seconds") ?: 300)
        private set
    var minutes by mutableIntStateOf(state.get<Int>("minutes") ?: 0)
        private set
    var accessibility by mutableStateOf(state.get<Boolean>("accessibility") ?: false)
        private set
    var isLockScreen by mutableStateOf(state.get<Boolean>("is_lock_screen") ?: false)
        private set
    var isStopMedia by mutableStateOf(state.get<Boolean>("is_stop_media") ?: false)
        private set
    var isGoHome by mutableStateOf(state.get<Boolean>("is_go_home") ?: false)
        private set
    var isDynamicColor by mutableStateOf(state.get<Boolean>("is_dynamic_color") ?: false)
        private set
    var isNotifPermission by mutableStateOf(state.get<Boolean>("is_notif_permission") ?: false)
        private set
    var isNoShowNotifPermission by mutableStateOf(state.get<Boolean>("is_no_show_notif") ?: false)
        private set

    init {
        viewModelScope.launch {
            allSettings.collect { settings ->
                isRunning = settings.isRunning
                if (!isRunning) {
                    leftSeconds = settings.leftSeconds
                }
            }
        }
    }

    // flow for sync data
    val getRunning = Prefs.runningFlow(context)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            isRunning
        )

    init {
        loadInitialSettings()
    }

    private fun loadInitialSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            val settings = Prefs.getAllSettings(context)

            withContext(Dispatchers.Main) {
                theme = state.get<ThemeMode>("theme") ?: settings.theme
                isRunning = state.get<Boolean>("is_running") ?: settings.isRunning
                leftSeconds = state.get<Int>("left_seconds") ?: settings.leftSeconds
                minutes = settings.minutes
                accessibility = settings.accessibility
                isLockScreen = settings.isLockScreen
                isStopMedia = settings.isStopMedia
                isGoHome = settings.isGoHome
                isDynamicColor = settings.isDynamicColor
                isNotifPermission = settings.isNotifPermission
                isNoShowNotifPermission = settings.isNoShowNotif
            }
        }
    }

    //function
    fun updateTheme(value: ThemeMode) {
        theme = value
        state["theme"] = value
        Prefs.saveTheme(context, value)
    }

    fun updateRunning(value: Boolean) {
        isRunning = value
        state["is_running"] = value
        Prefs.saveRunning(context, value)
    }

    fun updateLeftSeconds(value: Int) {
        leftSeconds = value
        state["left_seconds"] = value
        Prefs.saveLeftSeconds(context, value)
    }

    fun updateMinutes(value: Int) {
        minutes = value
        state["minutes"] = value
        Prefs.saveMinutes(context, value)
    }

    fun updateAccessibility(value: Boolean) {
        accessibility = value
        state["accessibility"] = value
        Prefs.saveAccessibility(context, value)
        if (isLockScreen && accessibility) updateLockScreen(true) else updateLockScreen(false)
    }

    fun updateLockScreen(value: Boolean) {
        isLockScreen = value
        state["is_lock_screen"] = value
        Prefs.saveLockScreen(context, value)
    }

    fun updateStopMedia(value: Boolean) {
        isStopMedia = value
        state["is_stop_media"] = value
        Prefs.saveStopMedia(context, value)
    }

    fun updateGoHome(value: Boolean) {
        isGoHome = value
        state["is_go_home"] = value
        Prefs.saveGoHome(context, value)
    }

    fun updateDynamicColor(value: Boolean) {
        isDynamicColor = value
        state["is_dynamic_color"] = value
        Prefs.saveDynamicColor(context, value)
    }

    fun updateNotifPermission(value: Boolean) {
        isNotifPermission = value
        state["is_notif_permission"] = value
        Prefs.saveNotifPermission(context, value)
    }

    fun updateNoShowNotifPermission(value: Boolean) {
        isNoShowNotifPermission = value
        state["is_no_show_notif"] = value
        Prefs.saveNoShowNotifPermission(context, value)
    }

    //timer logic and broadcast
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent?.action == "TIMER_TICK") {
                val seconds = intent.getIntExtra("time_left", 0)
                updateLeftSeconds(seconds)
                if (seconds <= 0) updateRunning(false)
            }
        }
    }

    private var isRegistered = false

    fun registerReceiver(context: Context) {
        if (isRegistered) return
        val filter = IntentFilter("TIMER_TICK")
        ContextCompat.registerReceiver(
            context.applicationContext,
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
        } catch (_: Exception) {
            Log.d("TIMER_VM", "safe ignore unregister")
        }
        isRegistered = false
    }

    fun startTimer(totalSeconds: Int) {
        val intent = Intent(application, TimerService::class.java).apply {
            putExtra("time", totalSeconds)
            action = TimerService.ACTION_START
        }
        ContextCompat.startForegroundService(context, intent)
        updateRunning(true)
    }

    fun setTimer(min: Int) {
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
        context.startService(intent)
    }
}
