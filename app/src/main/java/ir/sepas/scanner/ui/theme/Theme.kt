package ir.sepas.scanner.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Modern Material Design 3 colors for image scanner
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4F8FFF),
    onPrimary = Color(0xFF001E36),
    primaryContainer = Color(0xFF002E4E),
    onPrimaryContainer = Color(0xFFCBE6FF),
    secondary = Color(0xFF6FDBFF),
    onSecondary = Color(0xFF001F26),
    secondaryContainer = Color(0xFF004B57),
    onSecondaryContainer = Color(0xFF97F0FF),
    tertiary = Color(0xFFD4BBFF),
    onTertiary = Color(0xFF3A1D5D),
    tertiaryContainer = Color(0xFF523474),
    onTertiaryContainer = Color(0xFFEFDBFF),
    background = Color(0xFF0F1419),
    onBackground = Color(0xFFE1E2E8),
    surface = Color(0xFF0F1419),
    onSurface = Color(0xFFE1E2E8),
    surfaceVariant = Color(0xFF42474E),
    onSurfaceVariant = Color(0xFFC2C7CF)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006493),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCBE6FF),
    onPrimaryContainer = Color(0xFF001E36),
    secondary = Color(0xFF4F6079),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD2E4FF),
    onSecondaryContainer = Color(0xFF0B1D33),
    tertiary = Color(0xFF6B4C8C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEFDBFF),
    onTertiaryContainer = Color(0xFF251445),
    background = Color(0xFFFDFCFF),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFDFCFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDFE2EB),
    onSurfaceVariant = Color(0xFF42474E)
)

@Composable
fun AiScannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}