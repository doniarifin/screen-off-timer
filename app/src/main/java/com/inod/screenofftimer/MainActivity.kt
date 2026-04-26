package com.inod.screenofftimer

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.inod.screenofftimer.core.permission.NotificationPermission
import com.inod.screenofftimer.ui.navigation.AppNavGraph

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationPermission.request(this)
        installSplashScreen()

        val display = display
        val supportedModes = display?.supportedModes
        val highRefreshRateMode = supportedModes?.maxByOrNull { it.refreshRate }

        val params = window.attributes
        params.preferredDisplayModeId = highRefreshRateMode?.modeId ?: 0
        window.attributes = params

        enableEdgeToEdge()

        setContent {
            AppNavGraph()
        }
    }
}