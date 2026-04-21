package com.inod.screenofftimer.ui.screen

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.inod.screenofftimer.utils.lockScreen
import kotlinx.coroutines.delay

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.text.input.KeyboardType
import com.inod.screenofftimer.MainActivity
import com.inod.screenofftimer.ui.enums.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimersScreen(
    context: Context,
    themeMode: ThemeMode,
    onOpenSettings: () -> Unit
) {
//    var totalTime by remember { mutableStateOf(10) }
//    var timeLeft by remember { mutableStateOf(10) }
    var totalMinutes by remember { mutableIntStateOf(5) }
    var timeLeftSeconds by remember { mutableIntStateOf(totalMinutes * 60) }

    var isRunning by remember { mutableStateOf(false) }
//    timeLeftSeconds = totalMinutes * 60
    // Timer logic
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (timeLeftSeconds > 0) {
                timeLeftSeconds--
                delay(1000)
            }
            lockScreen(context)
            isRunning = false
        }
    }
    val totalSeconds = totalMinutes * 60
    val timeLeftTotal = timeLeftSeconds * totalMinutes
    val progress = (timeLeftSeconds / totalSeconds.toFloat()).coerceIn(0f, 1f)

    val minutes = timeLeftSeconds / 60
    val seconds = timeLeftSeconds % 60

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = ""
    )

    val color = when {
        timeLeftSeconds <= totalSeconds / 3 -> Color.Red
        timeLeftSeconds <= totalSeconds / 2 -> Color(0xFFFFA500) // orange
        else -> Color(0xFF4CAF50) // green
    }

    val isLightTheme = when (themeMode) {
        ThemeMode.LIGHT -> true
        ThemeMode.DARK -> false
        ThemeMode.SYSTEM -> !isSystemInDarkTheme()
    }

    var customInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize(),
//            .background(Color(0xFF0F172A)), // dark modern bg
        contentAlignment = Alignment.Center
    ) {
        Scaffold(
//            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Screen Off Timer",
                            color = if (isLightTheme) {
                                Color.White
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isLightTheme) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.Transparent
                        }
                    ),
                    actions = {
                        IconButton(
                            onClick = onOpenSettings
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = if (isLightTheme) {
                                    Color.White
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

//                Text(
//                    text = "Timer",
//                    style = MaterialTheme.typography.headlineMedium
//                )

                Icon(
                    modifier = Modifier
                        .size(40.dp),
                    imageVector = Icons.Default.Timer,
                    contentDescription = "timer",
                    tint = Color(0xFF4CAF50)
                )

                Spacer(modifier = Modifier.height(15.dp))

                // Progress + countdown (stack)
                Box(contentAlignment = Alignment.Center) {

                    CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(200.dp),
                    color = color,
                    strokeWidth = 10.dp,
                    trackColor = Color.DarkGray,
                    strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
                    )

//                    Text(
//                        text = "$timeLeft",
//                        color = color,
//                        style = MaterialTheme.typography.displayLarge
//                    )
                    Text(
                        text = "%02d:%02d".format(minutes, seconds),
                        color = color,
                        style = MaterialTheme.typography.displayLarge
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Preset time
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(1, 5, 10, 15).forEach { min ->

                        val isSelected = totalMinutes == min

                        OutlinedButton(
                            onClick = {
                                totalMinutes = min
                                timeLeftSeconds = min * 60
                                isRunning = false
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color.Transparent,

                                contentColor = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            ),
                            border = if (isSelected) null else ButtonDefaults.outlinedButtonBorder
                        ) {
                            Text("$min min")
                        }
                    }
                }

                OutlinedTextField(
                    value = customInput,
                    onValueChange = { customInput = it },
                    label = { Text("Custom (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Button(
                    onClick = {
                        val min = customInput.toIntOrNull()
                        if (min != null && min > 0) {
                            totalMinutes = min
                            timeLeftSeconds = min * 60
                            isRunning = false
                        }
                    }
                ) {
                    Text("Set")
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Controls
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                    Button(
                        onClick = {
                            timeLeftSeconds = totalMinutes * 60
                            isRunning = true
                        },
                        colors = ButtonDefaults.buttonColors(Color(0xFF4CAF50)),
                        enabled = !isRunning,
                    ) {
                        Text("Start")
                    }

                    OutlinedButton(
                        onClick = {
                            isRunning = false
                        }
                    ) {
                        Text("Stop")
                    }

                    TextButton(
                        onClick = {
                            isRunning = false
                            timeLeftSeconds = totalMinutes * 60
                        }
                    ) {
                        Text("Reset")
                    }
                }
            }
        }
    }
}
