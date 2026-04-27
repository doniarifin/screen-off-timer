package com.inod.screenofftimer.ui.navigation

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.inod.screenofftimer.ui.components.settings.LicenseScreen
import com.inod.screenofftimer.ui.enums.ThemeMode
import com.inod.screenofftimer.ui.screen.HomeScreen
import com.inod.screenofftimer.ui.screen.SettingsScreen
import com.inod.screenofftimer.ui.theme.ScreenOffTimerTheme
import com.inod.screenofftimer.viewmodel.TimerViewModel

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
@Composable
fun AppNavGraph() {

    val viewModel: TimerViewModel = viewModel()

    val context = LocalContext.current
    val navController = rememberNavController()
    val themeMode = viewModel.theme

    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val activity = LocalActivity.current as? ComponentActivity

    SideEffect {
        activity?.enableEdgeToEdge(
            statusBarStyle = if (isDark) {
                SystemBarStyle.dark(
                    scrim = android.graphics.Color.TRANSPARENT
                )
            } else {
                SystemBarStyle.light(
                    scrim = android.graphics.Color.TRANSPARENT,
                    darkScrim = android.graphics.Color.TRANSPARENT
                )
            }
        )
    }

    ScreenOffTimerTheme(darkTheme = isDark) {
        val colorBackground = MaterialTheme.colorScheme.background

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorBackground
        ) {
            NavHost(
                navController = navController,
                startDestination = "home",
                enterTransition = { fadeIn(animationSpec = tween(50)) + slideInHorizontally(initialOffsetX = { 100 }) },
                exitTransition = { fadeOut(animationSpec = tween(50)) + slideOutHorizontally(targetOffsetX = { -100 }) },
                popEnterTransition = { fadeIn(animationSpec = tween(50)) + slideInHorizontally(initialOffsetX = { -100 }) },
                popExitTransition = { fadeOut(animationSpec = tween(50)) + slideOutHorizontally(targetOffsetX = { 100 }) }
            ) {

                composable("home") {
                    HomeScreen(
                        context = context,
                        themeMode = themeMode,
                        onOpenSettings = {
                            navController.navigate("settings")
                        }
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        currentTheme = themeMode,
                        onThemeChange = { viewModel.updateTheme(it) },
                        onBack = {
                            navController.popBackStack()

                        },
                        onOpenLicenses = { navController.navigate("licenses") }
                    )
                }

                composable("licenses") {
                    LicenseScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

    }

}