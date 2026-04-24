package me.ajiew.bypasslistmaster.ui.theme

import android.app.Activity
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

private val LightScheme = lightColorScheme(
    primary = Color(0xFF0B5CAD),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E6FF),
    onPrimaryContainer = Color(0xFF001C3A),
    secondary = Color(0xFF2B6F74),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD2F0F1),
    onSecondaryContainer = Color(0xFF0A1F21),
    tertiary = Color(0xFF725188),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF4DAFF),
    onTertiaryContainer = Color(0xFF2A123F),
    background = Color(0xFFF6F8FC),
    onBackground = Color(0xFF171C24),
    surface = Color.White,
    onSurface = Color(0xFF171C24),
    surfaceVariant = Color(0xFFDEE3EC),
    onSurfaceVariant = Color(0xFF434A55),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFA8C9FF),
    onPrimary = Color(0xFF003061),
    primaryContainer = Color(0xFF004887),
    onPrimaryContainer = Color(0xFFD6E6FF),
    secondary = Color(0xFFB6DADD),
    onSecondary = Color(0xFF18393C),
    secondaryContainer = Color(0xFF2B5054),
    onSecondaryContainer = Color(0xFFD2F0F1),
    tertiary = Color(0xFFE0B8F8),
    onTertiary = Color(0xFF412257),
    tertiaryContainer = Color(0xFF58386F),
    onTertiaryContainer = Color(0xFFF4DAFF),
    background = Color(0xFF10141B),
    onBackground = Color(0xFFE2E7F0),
    surface = Color(0xFF171C24),
    onSurface = Color(0xFFE2E7F0),
    surfaceVariant = Color(0xFF434A55),
    onSurfaceVariant = Color(0xFFC3C9D3),
)

@Composable
fun BypassListMasterTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val context = LocalContext.current
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme ->
            dynamicDarkColorScheme(context)

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !darkTheme ->
            dynamicLightColorScheme(context)

        darkTheme -> DarkScheme
        else -> LightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
