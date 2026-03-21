package com.inventory.system.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFF2E7D32),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB8F5B1),
    error = Color(0xFFB00020),
    surface = Color(0xFFFFFBFE),
    background = Color(0xFFF5F5F5)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9ECAFF),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF004881),
    secondary = Color(0xFF9CD69F),
    onSecondary = Color(0xFF003909),
    secondaryContainer = Color(0xFF0B5B16),
    surface = Color(0xFF1C1B1F),
    background = Color(0xFF121212)
)

@Composable
fun InventoryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
