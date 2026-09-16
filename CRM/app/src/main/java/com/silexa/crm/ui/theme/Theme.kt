package com.silexa.crm.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SilexaGreen,
    secondary = SilexaGreenDark,
    tertiary = SilexaGreenLight,

    background = SilexaDarkBackground,
    surface = SilexaDarkSurface,

    onPrimary = SilexaWhite,
    onSecondary = SilexaWhite,
    onTertiary = SilexaBlack,

    onBackground = SilexaDarkText,
    onSurface = SilexaDarkText
)

private val LightColorScheme = lightColorScheme(
    primary = SilexaGreen,
    secondary = SilexaGreenDark,
    tertiary = SilexaGreen,

    background = SilexaBackground,
    surface = SilexaSurface,

    onPrimary = SilexaWhite,
    onSecondary = SilexaWhite,
    onTertiary = SilexaWhite,

    onBackground = SilexaTextPrimary,
    onSurface = SilexaTextPrimary
)

@Composable
fun CRMTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}