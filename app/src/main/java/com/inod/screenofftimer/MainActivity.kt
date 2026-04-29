package com.inod.screenofftimer

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inod.screenofftimer.data.prefs.Prefs
import com.inod.screenofftimer.service.TimerService
import com.inod.screenofftimer.ui.enums.ThemeMode
import com.inod.screenofftimer.viewmodel.TimerViewModel

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        window.setBackgroundDrawableResource(android.R.color.black)

        enableEdgeToEdge()

        setContent {
            val viewModel: TimerViewModel = viewModel()

            val settings by viewModel.allSettings.collectAsState()

            val isDark = when (settings.theme) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            // update system reactively
            DisposableEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    },
                    navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    }
                )
                onDispose {}
            }

            App()
        }
    }

    override fun onResume() {
        super.onResume()
        checkServiceAnomalies()
    }

    private fun checkServiceAnomalies() {
        val isSavedAsRunning = Prefs.isRunning(this)
        val isServiceActuallyRunning = TimerService.isServiceRunning

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val areNotificationsEnabled = notificationManager.areNotificationsEnabled()

        if (isSavedAsRunning && (!isServiceActuallyRunning || !areNotificationsEnabled)) {
            Prefs.saveRunning(this, false)
//            Prefs.saveLeftSeconds(this, 0)

            val stopIntent = Intent(this, TimerService::class.java).apply {
                action = TimerService.ACTION_STOP
            }
            startService(stopIntent)

            if (!areNotificationsEnabled) {
                Toast.makeText(
                    this,
                    "Please enable notifications for the timer to run properly.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}