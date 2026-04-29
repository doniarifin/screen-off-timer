package com.inod.screenofftimer.ui.screen

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.inod.screenofftimer.core.permission.AccessibilityPermission
import com.inod.screenofftimer.core.permission.NotificationPermission
import com.inod.screenofftimer.ui.components.SwitchStyle
import com.inod.screenofftimer.ui.components.settings.HorizontalSelected
import com.inod.screenofftimer.ui.components.settings.ListOption
import com.inod.screenofftimer.ui.components.settings.ListSection
import com.inod.screenofftimer.ui.enums.ThemeMode
import com.inod.screenofftimer.viewmodel.TimerViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    context: Context,
    viewModel: TimerViewModel,
    onBack: () -> Unit,
    onOpenLicenses: () -> Unit
) {
    BackHandler { onBack() }

    val activity = context as Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    // source from object
    val settings by viewModel.allSettings.collectAsState()

    // observer for realtime update
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val enabled = isAccessibilityEnabled(context)
                if (settings.accessibility != enabled) viewModel.updateAccessibility(enabled)

                val notifGranted = isNotifGranted(activity)
                if (settings.isNotifPermission != notifGranted) viewModel.updateNotifPermission(notifGranted)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val options = listOf(
        "Light" to ThemeMode.LIGHT,
        "Dark" to ThemeMode.DARK,
        "System" to ThemeMode.SYSTEM
    )

    val icons = listOf(
        Icons.Default.LightMode,
        Icons.Default.DarkMode,
        Icons.Default.BrightnessAuto
    )

    val isDynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            item {
                ListSection(title = "Theme", titleIcon = Icons.Outlined.Palette) {
                    ListOption(
                        onClick = {},
                        bgColor = Color.Transparent,
                        bottomContent = {
                            HorizontalSelected(
                                options = options.map { it.first },
                                icons = icons,
                                selectedIndex = options.indexOfFirst { it.second == settings.theme },
                                onSelect = { index ->
                                    viewModel.updateTheme(options[index].second)
                                }
                            )
                        }
                    )

                    ListOption(
                        bgColor = MaterialTheme.colorScheme.surfaceContainer,
                        title = "Dynamic color",
                        enabled = isDynamicColorSupported,
                        description = "Use color from your device",
                        clip = RoundedCornerShape(16.dp),
                        padding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        icon = Icons.Default.AutoAwesome,
                        onClick = {
                            if (isDynamicColorSupported) viewModel.updateDynamicColor(!settings.isDynamicColor)
                        },
                        trailing = {
                            SwitchStyle(
                                checked = settings.isDynamicColor,
                                enabled = isDynamicColorSupported,
                                onCheckedChange = { viewModel.updateDynamicColor(it) }
                            )
                        }
                    )
                }
            }

            // options
            item {
                ListSection(title = "Option", titleIcon = Icons.Outlined.Tune, padding = PaddingValues(top = 10.dp)) {
                    ListOption(
                        title = "Go Home",
                        description = "Go home after timer finished",
                        icon = Icons.Default.Home,
                        onClick = { viewModel.updateGoHome(!settings.isGoHome) },
                        trailing = {
                            SwitchStyle(
                                checked = settings.isGoHome,
                                onCheckedChange = { viewModel.updateGoHome(it) }
                            )
                        }
                    )
                }
            }

            // permission
            item {
                ListSection(title = "Permission", padding = PaddingValues(top = 10.dp), titleIcon = Icons.Default.Security) {
                    ListOption(
                        onClick = { openNotifPermission(activity) },
                        title = "Notification",
                        description = "Allow app to show timer and status notifications",
                        icon = Icons.Default.Notifications,
                        trailing = {
                            SwitchStyle(
                                checked = settings.isNotifPermission,
                                onCheckedChange = { openNotifPermission(activity) }
                            )
                        }
                    )

                    ListOption(
                        onClick = { openAccessibility(context) },
                        title = "Accessibility",
                        description = "Required to control screen off and system actions",
                        icon = Icons.Default.Accessibility,
                        trailing = {
                            SwitchStyle(
                                checked = settings.accessibility,
                                onCheckedChange = { openAccessibility(context) }
                            )
                        }
                    )
                }
            }

            // info
            item {
                ListSection(title = "Info", padding = PaddingValues(top = 10.dp, bottom = 10.dp), titleIcon = Icons.Outlined.Info) {
                    ListOption(
                        onClick = { onOpenLicenses() },
                        title = "Open source licenses",
                        description = "Open source libraries used in this app",
                        icon = Icons.Default.Code
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun openNotifPermission(activity: Activity) {
    NotificationPermission.openSettings(activity)
}

fun openAccessibility(context: Context) {
    AccessibilityPermission.open(context)
}

