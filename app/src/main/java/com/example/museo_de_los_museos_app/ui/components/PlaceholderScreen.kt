package com.example.museo_de_los_museos_app.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.museo_de_los_museos_app.R

/** Botón temporal de navegación para probar rutas mientras la pantalla real no existe. */
data class AccionPlaceholder(val texto: String, val alHacerClick: () -> Unit)

/**
 * Pantalla provisoria: la usan los placeholders de la carpeta ui/screens.
 * Cada responsable la reemplaza por su pantalla real y deja de usar este componente.
 */
@Composable
fun PlaceholderScreen(
    @StringRes titulo: Int,
    responsable: String,
    modifier: Modifier = Modifier,
    detalle: String? = null,
    acciones: List<AccionPlaceholder> = emptyList()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(titulo),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.placeholder_pendiente, responsable),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        if (detalle != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = detalle, style = MaterialTheme.typography.bodyMedium)
        }
        acciones.forEach { accion ->
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = accion.alHacerClick) { Text(accion.texto) }
        }
    }
}
