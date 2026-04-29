package com.inod.screenofftimer

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inod.screenofftimer.ui.navigation.AppNavGraph
import com.inod.screenofftimer.ui.theme.ScreenOffTimerTheme
import com.inod.screenofftimer.viewmodel.TimerViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun App() {
    val viewModel: TimerViewModel = viewModel()

    val settings by viewModel.allSettings.collectAsState()

    ScreenOffTimerTheme(
        themeMode = settings.theme,
        useDynamicColor = settings.isDynamicColor
    ) {
        AppNavGraph(
            viewModel = viewModel
        )
    }
}