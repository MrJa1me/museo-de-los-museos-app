package com.example.museo_de_los_museos_app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Botón principal de formularios. Con [cargando] = true se bloquea y muestra un indicador de progreso. */
@Composable
fun BotonPrincipal(
    texto: String,
    alHacerClick: () -> Unit,
    modifier: Modifier = Modifier,
    cargando: Boolean = false,
    habilitado: Boolean = true
) {
    Button(
        onClick = alHacerClick,
        enabled = habilitado && !cargando,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
        } else {
            Text(texto)
        }
    }
}
