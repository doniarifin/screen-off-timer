package com.inod.screenofftimer.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.inod.screenofftimer.ui.components.settings.LicenseScreen
import com.inod.screenofftimer.ui.screen.HomeScreen
import com.inod.screenofftimer.ui.screen.SettingsScreen
import com.inod.screenofftimer.viewmodel.TimerViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(
    viewModel: TimerViewModel
) {
    val context = LocalContext.current
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = "home",

            enterTransition = {
                fadeIn(animationSpec = tween(150)) + slideInHorizontally(initialOffsetX = { 150 })
            },
            exitTransition = {
                fadeOut(animationSpec = tween(150)) + slideOutHorizontally(targetOffsetX = { -150 })
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(150)) + slideInHorizontally(initialOffsetX = { -150 })
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(150)) + slideOutHorizontally(targetOffsetX = { 150 })
            }
        ) {
            composable("home") {
                HomeScreen(
                    context = context,
                    viewModel = viewModel,
                    onOpenSettings = { navController.navigate("settings") }
                )
            }

            composable("settings") {
                SettingsScreen(
                    context = context,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
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