package com.example.museo_de_los_museos_app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.museo_de_los_museos_app.ui.utils.idImagenMuseo

/**
 * Imagen del museo desde res/drawable. Si el archivo no existe (o la ficha no tiene imagen),
 * muestra un fondo con el ícono de museo en vez de cerrar la app.
 */
@Composable
private fun ImagenMuseoBase(
    imagen: String,
    descripcion: String,
    modifier: Modifier,
    contentScale: ContentScale,
    tamanoIcono: Dp
) {
    val context = LocalContext.current
    val idRecurso = remember(imagen) { context.idImagenMuseo(imagen) }

    if (idRecurso != 0) {
        Image(
            painter = painterResource(idRecurso),
            contentDescription = descripcion,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Museum,
                contentDescription = descripcion,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(tamanoIcono)
            )
        }
    }
}

/** Imagen completa a todo el ancho, para el encabezado del Detalle del museo. */
@Composable
fun ImagenMuseoEncabezado(imagen: String, descripcion: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val existe = remember(imagen) { context.idImagenMuseo(imagen) != 0 }
    ImagenMuseoBase(
        imagen = imagen,
        descripcion = descripcion,
        // FillWidth muestra la foto entera (sin recortes) respetando su proporción;
        // sin foto, el recuadro genérico necesita un alto fijo
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(if (existe) Modifier else Modifier.height(180.dp)),
        contentScale = ContentScale.FillWidth,
        tamanoIcono = 64.dp
    )
}

/** Miniatura cuadrada con bordes redondeados, para Favoritos y Resultado QR. */
@Composable
fun ImagenMuseoMiniatura(imagen: String, descripcion: String, modifier: Modifier = Modifier, tamano: Dp = 60.dp) {
    ImagenMuseoBase(
        imagen = imagen,
        descripcion = descripcion,
        modifier = modifier.size(tamano).clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop,
        tamanoIcono = tamano / 2
    )
}
