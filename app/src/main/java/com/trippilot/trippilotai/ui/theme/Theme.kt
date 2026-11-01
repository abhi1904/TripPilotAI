package com.trippilot.trippilotai.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Teal40,
    onPrimary = Color.White,
    primaryContainer = Teal90,
    onPrimaryContainer = Teal10,
    secondary = Teal50,
    onSecondary = Color.White,
    secondaryContainer = Teal95,
    onSecondaryContainer = Teal20,
    tertiary = Sand40,
    tertiaryContainer = Sand90,
    onTertiaryContainer = Color(0xFF2B1700),
    background = Mist,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE6F0EF),
    onSurfaceVariant = InkMuted,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8FCFC),
    surfaceContainer = Color(0xFFEFF6F6),
    surfaceContainerHigh = Color(0xFFE8F1F0),
    surfaceContainerHighest = Color(0xFFE1ECEB),
    outline = Outline,
    outlineVariant = Color(0xFFDCE6E5),
    error = Danger,
    errorContainer = DangerContainer,
)

private val DarkColorScheme = darkColorScheme(
    primary = Teal80,
    onPrimary = Teal20,
    primaryContainer = Teal30,
    onPrimaryContainer = Teal90,
    secondary = Teal80,
    onSecondary = Teal20,
    secondaryContainer = Color(0xFF1F3533),
    onSecondaryContainer = Teal90,
    tertiary = Sand80,
    tertiaryContainer = Color(0xFF5E3F00),
    onTertiaryContainer = Sand90,
    background = NightSurface,
    onBackground = Color(0xFFDDE5E4),
    surface = NightSurfaceHigh,
    onSurface = Color(0xFFDDE5E4),
    surfaceVariant = Color(0xFF26302F),
    onSurfaceVariant = Color(0xFFB5C3C2),
    surfaceContainerLowest = NightSurface,
    surfaceContainerLow = Color(0xFF161F1E),
    surfaceContainer = Color(0xFF1A2423),
    surfaceContainerHigh = Color(0xFF222D2C),
    surfaceContainerHighest = Color(0xFF2B3736),
    outline = Color(0xFF6F7F7E),
    outlineVariant = Color(0xFF3A4847),
)

val TripPilotShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/**
 * App theme. Dynamic color is off by default so the teal brand palette stays consistent
 * across devices; pass `dynamicColor = true` to experiment with Material You.
 */
@Composable
fun TripPilotAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = TripPilotShapes,
        content = content,
    )
}
