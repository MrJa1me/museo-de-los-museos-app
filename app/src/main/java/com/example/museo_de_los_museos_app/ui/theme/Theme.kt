package com.example.museo_de_los_museos_app.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = AzulMarinoClaro,
    onPrimary = AzulMarinoOscuro,
    primaryContainer = AzulMarino,
    onPrimaryContainer = AzulMarinoContenedor,
    secondary = DoradoClaro,
    onSecondary = DoradoOscuro,
    tertiary = BurdeosClaro,
    onTertiary = BurdeosOscuro,
    background = FondoNoche,
    onBackground = TextoClaro,
    surface = FondoNoche,
    onSurface = TextoClaro
)

private val LightColorScheme = lightColorScheme(
    primary = AzulMarino,
    onPrimary = Color.White,
    primaryContainer = AzulMarinoContenedor,
    onPrimaryContainer = AzulMarinoOscuro,
    secondary = Dorado,
    onSecondary = Color.White,
    secondaryContainer = DoradoContenedor,
    onSecondaryContainer = DoradoOscuro,
    tertiary = Burdeos,
    onTertiary = Color.White,
    tertiaryContainer = BurdeosContenedor,
    onTertiaryContainer = BurdeosOscuro,
    background = FondoMarfil,
    onBackground = TextoOscuro,
    surface = FondoMarfil,
    onSurface = TextoOscuro
)

@Composable
fun MuseodelosmuseosappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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