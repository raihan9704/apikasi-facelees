package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val StudioDarkColorScheme = darkColorScheme(
    primary = TechOrange,
    secondary = NeonPurple,
    tertiary = GlowCyan,
    background = NavyBackground,
    surface = DeepCardNavy,
    onPrimary = TextWhite,
    onSecondary = TextWhite,
    onTertiary = NavyBackground,
    onBackground = TextWhite,
    onSurface = TextWhite,
    outline = BorderNavy
)

private val StudioLightColorScheme = lightColorScheme(
    primary = TechOrange,
    secondary = NeonPurple,
    tertiary = GlowCyan,
    background = NavyBackground, // Keep premium dark theme even in light for cinematic feel!
    surface = DeepCardNavy,
    onPrimary = TextWhite,
    onSecondary = TextWhite,
    onTertiary = NavyBackground,
    onBackground = TextWhite,
    onSurface = TextWhite,
    outline = BorderNavy
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for the premium neon aesthetic!
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce branding
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) StudioDarkColorScheme else StudioLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
