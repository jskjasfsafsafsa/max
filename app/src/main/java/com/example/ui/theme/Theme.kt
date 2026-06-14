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

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = ActivePill,
    secondary = CardWellnessBg,
    tertiary = CardUtilityBg,
    background = Color(0xFF12140E), // High-comfort deep green-dark
    surface = Color(0xFF12140E),
    onBackground = Color(0xFFFCFAF5),
    onSurface = Color(0xFFFCFAF5),
    surfaceVariant = Color(0xFF23251F),
    onSurfaceVariant = Color(0xFFCCCCCC)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CardWellnessIcon,
    secondary = CardPrecisionIcon,
    tertiary = CardUtilityIcon,
    background = BackgroundTan,
    surface = BackgroundTan,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextDark,
    onSurface = TextDark,
    surfaceVariant = NavBg,
    onSurfaceVariant = TextMuted
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disabling dynamic colors by default to preserve custom Natural Tones theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
