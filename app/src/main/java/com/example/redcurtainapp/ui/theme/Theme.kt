package com.example.redcurtainapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Red80,
    secondary = RedGrey80,
    tertiary = Crimson80,
    background = Color(0xFF1A0000),
    surface = Color(0xFF2D0000),
    surfaceVariant = Color(0xFF3D0000),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFFFE5E5),
    onSurface = Color(0xFFFFE5E5),
    onSurfaceVariant = Color(0xFFFFCCCC)
)

private val LightColorScheme = lightColorScheme(
    primary = Red40,
    secondary = RedGrey40,
    tertiary = Crimson40,
    background = RedBackground,
    surface = RedSurface,
    surfaceVariant = Color(0xFFFFE8E8),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF2D0000),
    onSurface = Color(0xFF2D0000),
    onSurfaceVariant = Color(0xFF4D0000)
)

@Composable
fun RedCurtainAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled to maintain red theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}