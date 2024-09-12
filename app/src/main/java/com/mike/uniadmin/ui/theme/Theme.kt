package com.mike.uniadmin.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.mike.uniadmin.R


private val DarkColorScheme = darkColorScheme(
    primary = DarkNavy,
    secondary = DeepBlue,
    tertiary = BrightBlue,
    surface = LightGray,
    onPrimary = Turquoise,
    onSecondary = Black,
    surfaceContainer = containerDark


)

private val LightColorScheme = lightColorScheme(
    primary = LightBlue,
    secondary = PaleBlue,
    tertiary = Cyan,
    surface = DarkGray,
    onPrimary = SkyBlue,
    onSecondary = White,
    surfaceContainer = containerLight
)


val ChocoCooky = FontFamily(
    Font(R.font.chococooky, FontWeight.Normal),

    )

val CoolJaz = FontFamily(
    Font(R.font.cooljaz, FontWeight.Normal)
)

val DancingScript = FontFamily(
    Font(R.font.dancingscript, FontWeight.Normal)
)

val Caveat = FontFamily(
    Font(R.font.caveat, FontWeight.Normal)
)

@Composable
fun UniAdminTheme(
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}