package com.example.museo_de_los_museos_app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Avatar circular con la inicial del nombre. Cambia de color con animación al escribir un nombre.
 * Provisorio: cuando exista ImagenInteligente (cámara/galería) se usará la foto del usuario.
 */
@Composable
fun AvatarInicial(
    nombre: String,
    modifier: Modifier = Modifier,
    tamano: Dp = 88.dp
) {
    val inicial = nombre.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val colorFondo by animateColorAsState(
        targetValue = if (nombre.isBlank()) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        label = "colorAvatar"
    )

    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(colorFondo),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = inicial,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
