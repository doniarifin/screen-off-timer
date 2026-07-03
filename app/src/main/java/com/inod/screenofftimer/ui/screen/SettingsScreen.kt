package com.inod.screenofftimer.ui.screen

import android.annotation.SuppressLint
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.inod.screenofftimer.core.permission.AccessibilityPermission
import com.inod.screenofftimer.core.permission.NotificationPermission
import com.inod.screenofftimer.service.MyDeviceAdminReceiver
import com.inod.screenofftimer.service.isDpmActive
import com.inod.screenofftimer.service.requestDeviceAdmin
import com.inod.screenofftimer.ui.components.BackSource
import com.inod.screenofftimer.ui.components.ModalDialog
import com.inod.screenofftimer.ui.components.SwitchStyle
import com.inod.screenofftimer.ui.components.settings.HorizontalSelected
import com.inod.screenofftimer.ui.components.settings.ListOption
import com.inod.screenofftimer.ui.components.settings.ListSection
import com.inod.screenofftimer.ui.enums.ThemeMode
import com.inod.screenofftimer.viewmodel.TimerViewModel
import androidx.core.net.toUri
import com.inod.screenofftimer.service.removeDeviceAdmin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    context: Context,
    viewModel: TimerViewModel,
    onBack: () -> Unit,
    navController: NavController
) {
//    BackHandler { onBack() }

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

    var showDialogAdmin by remember { mutableStateOf(false) }
    val deviceAdmin = settings.deviceAdmin

    val handleToggleAdmin: (Boolean) -> Unit = { value ->
        viewModel.updateDeviceAdmin(value)
        if (value) {
            val dpmActive = isDpmActive(context)
            when {
                dpmActive -> {
                    viewModel.updateDeviceAdmin(true)
                }
                else -> {
                    showDialogAdmin = true
                }
            }
        } else {
            removeDeviceAdmin(context)
            viewModel.updateDeviceAdmin(false)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val dpmActive = isDpmActive(context)
                viewModel.updateDeviceAdmin(dpmActive)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
                    IconButton(onClick = {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("backSource", BackSource.CLICK.name)
                        navController.popBackStack()
                    }) {
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
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
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

                    ListOption(
                        onClick = { handleToggleAdmin(!deviceAdmin) },
                        title = "Device Admin",
                        description = "Lock screen via device admin permission, disable this before uninstalling the app",
                        icon = Icons.Default.AdminPanelSettings,
                        trailing = {
                            SwitchStyle(
                                checked = deviceAdmin,
                                onCheckedChange = handleToggleAdmin
                            )
                        }
                    )
                }
            }

            // info
            item {
                ListSection(title = "Info", padding = PaddingValues(top = 10.dp, bottom = 10.dp), titleIcon = Icons.Outlined.Info) {
                    ListOption(
                        onClick = { openUrl(context, "https://ko-fi.com/inodxx") },
                        title = "Donate",
                        description = "Support me :)",
                        icon = Icons.Default.Favorite
                    )

                    ListOption(
                        onClick = { navController.navigate("licenses") },
                        title = "Open source licenses",
                        description = "Open source libraries used in this app",
                        icon = Icons.Default.Code
                    )
                }
            }
        }

        val descriptionText = buildAnnotatedString {
            append("Device Admin permission is used to lock the device\n\n")
            withStyle(style = SpanStyle(fontSize = 12.sp)) {
                append("* No collect, store, or share any personal information.\n")
                append("* Disable this permission, if you want to uninstall the app.")
            }
        }

        //modal dialog admin
        ModalDialog(
            show = showDialogAdmin,
            title = "Device Admin Permission",
            description = descriptionText,
            confirmText = "Agree",
            dismissText = "Cancel",
            onConfirm = {
                showDialogAdmin = false
                requestDeviceAdmin(context)
            },
            onDismiss = {
                showDialogAdmin = false
                val dpmActive = isDpmActive(context)
                viewModel.updateDeviceAdmin(dpmActive)
            },
        )
    }
}

fun openNotifPermission(activity: Activity) {
    NotificationPermission.openSettings(activity)
}

fun openAccessibility(context: Context) {
    AccessibilityPermission.open(context)
}

@SuppressLint("UseKt")
fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}


