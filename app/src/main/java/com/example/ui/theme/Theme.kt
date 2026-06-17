package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  lightColorScheme(
    primary = VibrantTealPrimary,
    secondary = VibrantEmerald,
    tertiary = VibrantVioletText,
    background = VibrantBgSlate,
    surface = VibrantCardBg,
    onPrimary = Color.White,
    onSecondary = VibrantTextDark,
    onTertiary = VibrantTextDark,
    onBackground = VibrantTextDark,
    onSurface = VibrantTextDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = VibrantTealPrimary,
    secondary = VibrantEmerald,
    tertiary = VibrantVioletText,
    background = VibrantBgSlate,
    surface = VibrantCardBg,
    onPrimary = Color.White,
    onSecondary = VibrantTextDark,
    onTertiary = VibrantTextDark,
    onBackground = VibrantTextDark,
    onSurface = VibrantTextDark
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
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
