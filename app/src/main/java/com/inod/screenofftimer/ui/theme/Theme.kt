package com.inod.screenofftimer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.inod.screenofftimer.ui.enums.ThemeMode

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF86BDEB),
    onPrimary = Color(0xFF0A1A24),

    primaryContainer = Color(0xFF12324A),
    onPrimaryContainer = Color(0xFFCDE6FF),

    secondary = Color(0xFF9BB7CC),
    onSecondary = Color(0xFF0A141A),

    secondaryContainer = Color(0xFF1D2832),
    onSecondaryContainer = Color(0xFFE7F2FA),

    tertiary = Color(0xFFAAB9C6),
    onTertiary = Color(0xFF0A1218),

    tertiaryContainer = Color(0xFF202830),
    onTertiaryContainer = Color(0xFFEFF6FB),

    background = Color(0xFF0A0A0C),
    onBackground = Color(0xFFE6E6E6),

    surface = Color(0xFF121214),
    onSurface = Color(0xFFE6E6E6),

    surfaceVariant = Color(0xFF1A1A1D),
    onSurfaceVariant = Color(0xFFB5B5B5),

    surfaceContainerLowest = Color(0xFF0A0A0C),
    surfaceContainerLow = Color(0xFF111114),
    surfaceContainer = Color(0xFF151518),
    surfaceContainerHigh = Color(0xFF1C1C20),
    surfaceContainerHighest = Color(0xFF242429),

    outline = Color(0xFF5A6470)
)

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1E88E5),
    onPrimary = Color(0xFFFFFFFF),

    primaryContainer = Color(0xFF9FCCFF),
    onPrimaryContainer = Color(0xFF001D34),

    secondary = Color(0xFF0D47A1),
    onSecondary = Color(0xFFFFFFFF),

    secondaryContainer = Color(0xFFD2E4FF),
    onSecondaryContainer = Color(0xFF001C37),

    tertiary = Color(0xFF1976D2),
    onTertiary = Color(0xFFFFFFFF),

    tertiaryContainer = Color(0xFFD1E4FF),
    onTertiaryContainer = Color(0xFF001D33),

    background = Color(0xFFF5F9FF),
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1E),

    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF44546A),

    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3F6FB),
    surfaceContainer = Color(0xFFEAF1F9),
    surfaceContainerHigh = Color(0xFFDCE7F3),
    surfaceContainerHighest = Color(0xFFCFE0F0),

    outline = Color(0xFF7A8AA0)

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun ScreenOffTimerTheme(
    themeMode: ThemeMode,
    useDynamicColor: Boolean,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val context = LocalContext.current

    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}