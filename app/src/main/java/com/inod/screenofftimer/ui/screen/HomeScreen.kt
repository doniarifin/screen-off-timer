package com.inod.screenofftimer.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inod.screenofftimer.service.MediaControlAccessibilityService
import com.inod.screenofftimer.ui.components.settings.ListOption
import com.inod.screenofftimer.ui.components.ModalDialog
import com.inod.screenofftimer.ui.components.SwitchStyle
import com.inod.screenofftimer.ui.components.settings.ListSection
import com.inod.screenofftimer.ui.components.timer.PresetTime
import com.inod.screenofftimer.ui.components.timer.TimerProgress
import com.inod.screenofftimer.ui.enums.ThemeMode
import com.inod.screenofftimer.viewmodel.TimerViewModel

@SuppressLint("ViewModelConstructorInComposable")
@RequiresApi(Build.VERSION_CODES.BAKLAVA)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    context: Context, themeMode: ThemeMode, onOpenSettings: () -> Unit
) {

    val viewModel: TimerViewModel = viewModel()

    val isRunning by viewModel.getRunning.collectAsState()
    val timeLeftSeconds = viewModel.leftSeconds

    val isLightTheme = when (themeMode) {
        ThemeMode.LIGHT -> true
        ThemeMode.DARK -> false
        ThemeMode.SYSTEM -> !isSystemInDarkTheme()
    }

    val focusManager = LocalFocusManager.current

    //options
    val isStopMedia = viewModel.isStopMedia
    val lockScreenEnabled = viewModel.isLockScreen

    LaunchedEffect(Unit) {
        val enabled = isAccessibilityEnabled(context)
        viewModel.updateAccessibility(enabled)
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val enabled = isAccessibilityEnabled(context)
                viewModel.updateAccessibility(enabled)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(Unit) {
        viewModel.registerReceiver(context)

        onDispose {
            viewModel.unregisterReceiver(context)
        }
    }

    var showEnableAccessibilityDialog by remember { mutableStateOf(false) }

    val handleToggle: (Boolean) -> Unit = { value ->
        viewModel.updateLockScreen(value)
        if (value && !isAccessibilityEnabled(context)) {
            showEnableAccessibilityDialog = true
        } else {
            viewModel.updateLockScreen(value)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focusManager.clearFocus()
                    })
            }
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Screen Off Timer",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }, colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isLightTheme) {
                            Color.Transparent
                        } else {
                            Color.Transparent
                        }
                    ), actions = {
                        IconButton(
                            onClick = onOpenSettings
                        ) {
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
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(15.dp))

                // Progress + countdown
                TimerProgress()

                Spacer(modifier = Modifier.height(20.dp))

                Spacer(modifier = Modifier.height(10.dp))

                // Preset time
                PresetTime()

                Spacer(modifier = Modifier.height(10.dp))

                Spacer(modifier = Modifier.height(5.dp))

                FilledIconButton(
                    onClick = {
                        if (isRunning) viewModel.stop(timeLeftSeconds)
                        else viewModel.startTimer(timeLeftSeconds)
                    },
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isRunning) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isRunning) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    AnimatedContent(
                        targetState = isRunning, label = "icon_anim"
                    ) { running ->
                        Icon(
                            imageVector = if (running) Icons.Default.Stop else Icons.Default.PlayArrow,
                            modifier = Modifier.size(40.dp),
                            contentDescription = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                //options
                ListSection() {
                    ListOption(
                        title = "Turn Off Music",
                        description = "All media will be turn off",
                        enabled = !isRunning,
                        icon = Icons.Default.MusicOff,
                        contentIcon = "Media off",
                        onClick = {
                            viewModel.updateStopMedia(!isStopMedia)
                        },
                        trailing = {
                            SwitchStyle(
                                checked = isStopMedia, enabled = !isRunning, onCheckedChange = {
                                    viewModel.updateStopMedia(it)
                                })
                        })

                    ListOption(
                        title = "Lock Screen",
                        description = "Automatically lock the device screen",
                        enabled = !isRunning,
                        icon = Icons.Default.Lock,
                        contentIcon = "Media off",
                        onClick = {
                            handleToggle(!lockScreenEnabled)
                        },
                        trailing = {
                            SwitchStyle(
                                checked = lockScreenEnabled,
                                enabled = !isRunning,
                                onCheckedChange = handleToggle
                            )
                        })
                }
            }

            //modal dialog
            ModalDialog(
                showEnableAccessibilityDialog,
                "Accessibility Required",
                "Enable Accessibility for this feature.",
                "Agree",
                "Cancel",
                {
                    showEnableAccessibilityDialog = false
                    openAccessibilitySettings(context)
                },
                {
                    showEnableAccessibilityDialog = false

                    val enabled = isAccessibilityEnabled(context)
                    viewModel.updateAccessibility(enabled)
                })
        }
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