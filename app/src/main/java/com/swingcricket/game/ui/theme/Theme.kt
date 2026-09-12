package com.swingcricket.game.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SwingCricketColorScheme = darkColorScheme(
    primary = PitchGreen,
    onPrimary = StadiumNight,
    secondary = FloodlightGold,
    onSecondary = StadiumNight,
    tertiary = SkyBlue,
    background = StadiumNight,
    onBackground = TextPrimary,
    surface = StadiumCard,
    onSurface = TextPrimary,
    surfaceVariant = StadiumNightElevated,
    onSurfaceVariant = TextSecondary,
    outline = StadiumOutline,
    error = WicketRed,
    onError = TextPrimary,
)

@Composable
fun SwingCricketTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = StadiumNight.toArgb()
            window.navigationBarColor = StadiumNight.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = SwingCricketColorScheme,
        typography = SwingCricketTypography,
        content = content
    )
}
