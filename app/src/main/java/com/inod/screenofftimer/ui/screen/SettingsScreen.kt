package com.inod.screenofftimer.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inod.screenofftimer.ui.components.SwitchStyle
import com.inod.screenofftimer.ui.components.settings.HorizontalSelected
import com.inod.screenofftimer.ui.components.settings.ListOption
import com.inod.screenofftimer.ui.components.settings.ListSection
import com.inod.screenofftimer.ui.enums.ThemeMode
import com.inod.screenofftimer.viewmodel.TimerViewModel


class Settings {
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    onBack: () -> Unit,
    onOpenLicenses: () -> Unit
) {

    BackHandler {
        onBack()
    }

    val viewModel: TimerViewModel = viewModel()

    //options
    val isGoHome = viewModel.isGoHome

    val isLightTheme = when (currentTheme) {
        ThemeMode.LIGHT -> true
        ThemeMode.DARK -> false
        ThemeMode.SYSTEM -> !isSystemInDarkTheme()
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Settings",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ListSection(title = "Theme", titleIcon = Icons.Outlined.Palette) {
                    ListOption(
                        onClick = {},
                        bgColor = MaterialTheme.colorScheme.background,
                        bottomContent = {
                            HorizontalSelected(
                                options = options.map { it.first },
                                icons = icons,
                                selectedIndex = options.indexOfFirst { it.second == currentTheme },
                                onSelect = { index ->
                                    onThemeChange(options[index].second)
                                }
                            )
                        }
                    )

                }

                Spacer(modifier = Modifier.width(10.dp))

                ListSection(title = "Option", titleIcon = Icons.Outlined.Tune) {
                    ListOption(
                        bgColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        title = "Go Home",
                        description = "Go home after timer finished",
                        padding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        icon = Icons.Default.Home,
                        onClick = { viewModel.updateGoHome(!isGoHome) },
                        trailing = {
                            SwitchStyle(
                                checked = isGoHome,
                                onCheckedChange = {
                                    viewModel.updateGoHome(it)
                                }
                            )
                        }

                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                ListSection(title = "Info", padding = PaddingValues(top = 10.dp, bottom = 10.dp), titleIcon = Icons.Outlined.Info) {
                    ListOption(
                        onClick = { onOpenLicenses() },
                        padding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                        bgColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        title = "Open source licenses",
                        description = "Open source libraries used in this app",
                        icon = Icons.Default.Code
                    )
                }


            }
        }
    }
}
