package com.inod.screenofftimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.inod.screenofftimer.ui.enums.ThemeMode
import com.inod.screenofftimer.ui.screen.SettingsScreen
import com.inod.screenofftimer.ui.screen.TimersScreen
import com.inod.screenofftimer.ui.theme.ScreenOffTimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }



            val isDark = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            ScreenOffTimerTheme(darkTheme = isDark) {

                NavHost(
                    navController = navController,
                    startDestination = "timer",
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
                    },
                    popEnterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        )
                    }
                ) {

                    composable("timer") {
                        TimersScreen(
                            context = this@MainActivity,
                            onOpenSettings = {
                                navController.navigate("settings")
                            }
                        )
                    }

                    composable("settings") {
                        SettingsScreen(
                            currentTheme = themeMode,
                            onThemeChange = { themeMode = it },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}