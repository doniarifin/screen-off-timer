package com.inod.screenofftimer.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.SwitchDefaults
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
import com.inod.screenofftimer.ui.components.ListOption
import com.inod.screenofftimer.ui.components.ModalDialog
import com.inod.screenofftimer.ui.components.timer.PresetTime
import com.inod.screenofftimer.ui.components.timer.TimerProgress
import com.inod.screenofftimer.ui.enums.ThemeMode
import com.inod.screenofftimer.viewmodel.TimerViewModel

@SuppressLint("ViewModelConstructorInComposable")
@RequiresApi(Build.VERSION_CODES.BAKLAVA)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    context: Context,
    themeMode: ThemeMode,
    onOpenSettings: () -> Unit
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focusManager.clearFocus()
                    }
                )
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isLightTheme) {
                            Color.Transparent
                        } else {
                            Color.Transparent
                        }
                    ),
                    actions = {
                        IconButton(
                            onClick = onOpenSettings
                        ) {
                            Icon(
                                modifier = Modifier
                                    .size(25.dp),
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        ) { padding ->

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
                        containerColor = if (isRunning)
                            MaterialTheme.colorScheme.surfaceVariant
                        else
                            MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isRunning)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    AnimatedContent(
                        targetState = isRunning,
                        label = "icon_anim"
                    ) { running ->
                        Icon(
                            imageVector = if (running) Icons.Default.Stop else Icons.Default.PlayArrow,
                            modifier = Modifier.size(40.dp),
                            contentDescription = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                //options
                ListOption(title = "Options", titleIcon = Icons.Default.Tune) {
                    SwitchSettingItem(
                        title = "Turn Off Music",
                        checked = isStopMedia,
                        enabled = !isRunning,
                        icon = Icons.Default.MusicOff,
                        contentIcon = "Music Off",
                        description = "All Media will be turn off",
                        onToggle = { newValue ->
                            viewModel.updateStopMedia(newValue)
                        }
                    )

                    SwitchSettingItem(
                        title = "Lock Screen",
                        checked = lockScreenEnabled,
                        enabled = !isRunning,
                        icon = Icons.Default.Lock,
                        contentIcon = "Lockscreen",
                        description = "The Phone will be lockscreen as normally lockscreen",
                        onToggle = { newValue ->
                            viewModel.updateLockScreen(newValue)
                            if (newValue) {
                                if (!isAccessibilityEnabled(context)) {
                                    showEnableAccessibilityDialog = true
                                } else {
                                    viewModel.updateLockScreen(true)
                                }
                            } else {
                                viewModel.updateLockScreen(false)
                            }
                        }
                    )
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
                }
            )
        }
    }
}

@Composable
fun SwitchSettingItem(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    icon: ImageVector,
    contentIcon: String,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onToggle(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = icon,
            contentDescription = contentIcon,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = { onToggle(it) },
            thumbContent = {
                if (checked) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize)
                    )
                }
            }
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