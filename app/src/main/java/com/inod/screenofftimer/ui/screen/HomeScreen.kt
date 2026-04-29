package com.inod.screenofftimer.ui.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.inod.screenofftimer.core.permission.NotificationPermission
import com.inod.screenofftimer.service.MediaControlAccessibilityService
import com.inod.screenofftimer.ui.components.ModalDialog
import com.inod.screenofftimer.ui.components.SwitchStyle
import com.inod.screenofftimer.ui.components.settings.ListOption
import com.inod.screenofftimer.ui.components.settings.ListSection
import com.inod.screenofftimer.ui.components.timer.PresetTime
import com.inod.screenofftimer.ui.components.timer.TimerProgress
import com.inod.screenofftimer.viewmodel.TimerViewModel

//@SuppressLint("ViewModelConstructorInComposable")
//@RequiresApi(Build.VERSION_CODES.BAKLAVA)
//@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    context: Context,  viewModel: TimerViewModel, onOpenSettings: () -> Unit
) {

    val activity = context as Activity

    val settings by viewModel.allSettings.collectAsState()

    //get data from setting object
    val isRunning = settings.isRunning
    val isStopMedia = settings.isStopMedia
//    val lockScreenEnabled = settings.isLockScreen

    // get from viewmodel directly
    val timeLeftSeconds = viewModel.leftSeconds
    val lockScreenEnabled = viewModel.isLockScreen

    val focusManager = LocalFocusManager.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val enabled = isAccessibilityEnabled(context)
                viewModel.updateAccessibility(enabled)

//                if (viewModel.accessibility != enabled) {
//                    viewModel.updateAccessibility(enabled)
//                }

                val granted = isNotifGranted(activity)
                if (viewModel.isNotifPermission != granted) {
                    viewModel.updateNotifPermission(granted)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // sign the receiver broadcast
    DisposableEffect(Unit) {
        viewModel.registerReceiver(context)
        onDispose { viewModel.unregisterReceiver(context) }
    }

    var showEnableAccessibilityDialog by remember { mutableStateOf(false) }
    var showNotifPermissionDialog by remember { mutableStateOf(false) }

    val handleToggleLockscreen: (Boolean) -> Unit = { value ->
        viewModel.updateLockScreen(value)
        if (value && !isAccessibilityEnabled(context)) {
            showEnableAccessibilityDialog = true
        }
//        else {
//            viewModel.updateLockScreen(value)
//        }
    }

    val handleNotifStart: () -> Unit = let@{
        if (isRunning) {
            viewModel.stop(timeLeftSeconds)
            return@let
        }

        viewModel.startTimer(timeLeftSeconds)

        val isNotifGranted = isNotifGranted(activity)
        val notShowAsk = viewModel.isNoShowNotifPermission

        if (!isNotifGranted) {
            if (!notShowAsk) {
                showNotifPermissionDialog = true
            } else {
                showToastProperly(context)
            }
        }
    }

    val handleNotShowAgain: () -> Unit = {
        viewModel.updateNoShowNotifPermission(true)
        showNotifPermissionDialog = false
        showToastProperly(context)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Screen Off Timer",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            modifier = Modifier.size(25.dp),
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                })
        }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(15.dp))
            TimerProgress(viewModel)
            Spacer(modifier = Modifier.height(10.dp))
            PresetTime(viewModel)
            Spacer(modifier = Modifier.height(10.dp))

            FilledIconButton(
                onClick = { handleNotifStart() },
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(24.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isRunning) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isRunning) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                AnimatedContent(targetState = isRunning, label = "icon_anim") { running ->
                    Icon(
                        imageVector = if (running) Icons.Default.Stop else Icons.Default.PlayArrow,
                        modifier = Modifier.size(40.dp),
                        contentDescription = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            ListSection {
                ListOption(
                    title = "Turn Off Music",
                    description = "All media will be turn off",
                    enabled = !isRunning,
                    icon = Icons.Default.MusicOff,
                    onClick = { viewModel.updateStopMedia(!isStopMedia) },
                    trailing = {
                        SwitchStyle(
                            checked = isStopMedia,
                            enabled = !isRunning,
                            onCheckedChange = { viewModel.updateStopMedia(it) })
                    })
                ListOption(
                    title = "Lock Screen",
                    description = "Automatically lock the device screen",
                    enabled = !isRunning,
                    icon = Icons.Default.Lock,
                    onClick = { handleToggleLockscreen(!lockScreenEnabled) },
                    trailing = {
                        SwitchStyle(
                            checked = lockScreenEnabled,
                            enabled = !isRunning,
                            onCheckedChange = handleToggleLockscreen
                        )
                    })
            }
        }

        //modal dialog access
        ModalDialog(
            show = showEnableAccessibilityDialog,
            title = "Accessibility Required",
            description = "Accessibility Service permission is required for the automated screen lock feature. \n" +
                    "\n" +
                    "* This service operates locally on your device and does not collect, store, or share any personal information.",
            confirmText = "Agree",
            dismissText = "Cancel",
            onConfirm = {
                showEnableAccessibilityDialog = false
                openAccessibilitySettings(context)
            },
            onDismiss = {
                showEnableAccessibilityDialog = false

                val enabled = isAccessibilityEnabled(context)
                viewModel.updateAccessibility(enabled)
            })

        //modal dialog notif
        ModalDialog(
            show = showNotifPermissionDialog,
            title = "Notification Permission Required",
            description = "Notification permission is required to display real-time countdown updates " +
                    "and ensure the timer runs reliably in the background, " +
                    "please enable notifications.",
            confirmText = "Agree",
            dismissText = "Cancel",
            onConfirm = {
                showNotifPermissionDialog = false
                openNotifSetting(activity)
            },
            onDismiss = {
                showNotifPermissionDialog = false
                if (!isNotifGranted(activity)) {
                    showToastProperly(context)
                }
            },
            leftButton = "Don't show again",
            onLeftButton = { handleNotShowAgain() },
        )
    }
}

fun isAccessibilityEnabled(context: Context): Boolean {
    val am =
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager

    val enabledServices = am.getEnabledAccessibilityServiceList(
        android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK
    )

    return enabledServices.any {
        it.resolveInfo.serviceInfo.name.contains(
            MediaControlAccessibilityService::class.java.name
        )
    }
}

fun openAccessibilitySettings(context: Context) {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

fun openNotifSetting(activity: Activity) {
    NotificationPermission.request(activity)
}

fun isNotifGranted(activity: Activity): Boolean {
    return NotificationPermission.isGranted(activity)
}

private fun showToastProperly(context: Context) {
    Toast.makeText(
        context,
        "Please enable notifications for the timer to run properly.",
        Toast.LENGTH_LONG
    ).show()
}
