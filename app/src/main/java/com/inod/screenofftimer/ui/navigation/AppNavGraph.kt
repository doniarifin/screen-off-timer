package com.inod.screenofftimer.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.inod.screenofftimer.ui.components.BackSource
import com.inod.screenofftimer.ui.components.PredictiveBackContainer
import com.inod.screenofftimer.ui.screen.LicenseScreen
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

    val currentEntry by navController.currentBackStackEntryAsState()

    val previousRoute = remember(currentEntry) {
        navController.previousBackStackEntry?.destination?.route
    }

    val screenContent: @Composable (String) -> Unit = { route ->
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            when (route) {
                "home" -> HomeScreen(
                    context = context,
                    viewModel = viewModel,
                    onOpenSettings = { navController.navigate("settings") }
                )
                "settings" -> SettingsScreen(
                    context = context,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack()  },
//                    previousRoute = previousRoute,
                    navController = navController
                )
                "licenses" -> LicenseScreen(
                    navController = navController,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = "home",
            enterTransition = {
                fadeIn(animationSpec = tween(250)) +
                        slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth })
            },
            exitTransition = {
                fadeOut(animationSpec = tween(250)) +
                        slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth })
            },

            popEnterTransition = {
                val source = initialState.savedStateHandle
                    .get<String>("backSource")
                    ?.let { BackSource.valueOf(it) }
                when (source) {
                    BackSource.GESTURE -> {
                        scaleIn(
                            initialScale = 0.85f,
                            transformOrigin = TransformOrigin(0f, 0.5f),
                        ) + slideInHorizontally(
                            initialOffsetX = { fullWidth -> -fullWidth / 8 },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(300))
                    }
                    BackSource.CLICK, null -> {
                        fadeIn(animationSpec = tween(150)) +
                                slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth / 4 })
                    }
                }
            },

            popExitTransition = {
                val source = initialState.savedStateHandle
                    .get<String>("backSource")
                    ?.let { BackSource.valueOf(it) }
                when (source) {
                    BackSource.GESTURE -> {
                        scaleOut(
                            targetScale = 0.9f,
                            transformOrigin = TransformOrigin(1f, 0.5f),
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) + slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth / 8 },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(300))
                    }
                    BackSource.CLICK, null -> {
                        fadeOut(animationSpec = tween(150)) +
                                slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth })
                    }
                }
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
                PredictiveBackContainer(
                    navController = navController,
                    previousContent = previousRoute?.let { route -> { screenContent(route) } },
                    onDismiss = { navController.popBackStack() }
                ) {
                    SettingsScreen(
                        context = context,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack()  },
                        navController = navController
                    )
                }
            }

            composable("licenses") {
            PredictiveBackContainer(
                navController = navController,
                previousContent = previousRoute?.let { route -> { screenContent(route) } },
                onDismiss = { navController.popBackStack() }
            ) {
                LicenseScreen(
                    navController = navController,
                    onBack = { navController.popBackStack() },
                )
            }

            }
        }
    }
}