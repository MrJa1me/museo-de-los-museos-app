package com.example.museo_de_los_museos_app.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.museo_de_los_museos_app.ui.utils.cargarImagenReducida
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Foto de perfil circular. Si no hay imagen (o no se pudo leer), muestra un ícono de persona. */
@Composable
fun ImagenInteligente(
    uri: Uri?,
    descripcion: String,
    modifier: Modifier = Modifier,
    tamano: Dp = 120.dp
) {
    val context = LocalContext.current
    var imagen by remember { mutableStateOf<Bitmap?>(null) }

    // Cada vez que cambia la uri, se lee la imagen fuera del hilo principal.
    LaunchedEffect(uri) {
        imagen = if (uri == null) null else withContext(Dispatchers.IO) {
            context.cargarImagenReducida(uri, ladoMaximo = 512)
        }
    }

    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = imagen
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = descripcion,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(tamano)
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = descripcion,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(tamano / 2)
            )
        }
    }
}