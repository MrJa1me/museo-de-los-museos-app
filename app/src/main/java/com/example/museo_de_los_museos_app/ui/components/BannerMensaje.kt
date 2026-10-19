package com.example.museo_de_los_museos_app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Mensaje de retroalimentación (éxito o error) de un formulario.
 * Se usa dentro de un AnimatedVisibility para que aparezca y desaparezca con animación.
 */
@Composable
fun BannerMensaje(
    texto: String,
    esError: Boolean,
    modifier: Modifier = Modifier
) {
    val colorFondo = if (esError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val colorContenido = if (esError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = colorFondo,
        contentColor = colorContenido
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (esError) Icons.Filled.Error else Icons.Filled.CheckCircle,
                contentDescription = null
            )
            Text(text = texto, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
