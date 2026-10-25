package com.example.museo_de_los_museos_app.ui.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.core.content.FileProvider
import java.io.File

/** Archivo nuevo para la foto de perfil, en la carpeta privada "avatares/". */
fun Context.crearArchivoAvatar(userId: String): File {
    val carpeta = File(filesDir, "avatares")
    carpeta.mkdirs()
    return File(carpeta, "${userId}_${System.currentTimeMillis()}.jpg")
}

/** Dirección content:// que la app de cámara puede usar para escribir en [archivo]. */
fun Context.uriCompartible(archivo: File): Uri =
    FileProvider.getUriForFile(this, "$packageName.fileprovider", archivo)

/**
 * Copia la imagen elegida en la galería a nuestra carpeta privada.
 * Así la foto sigue existiendo aunque el permiso temporal de la galería se pierda.
 */
fun Context.copiarAvatar(uri: Uri, userId: String): Uri? = try {
    val destino = crearArchivoAvatar(userId)
    contentResolver.openInputStream(uri)?.use { entrada ->
        destino.outputStream().use { salida -> entrada.copyTo(salida) }
    }
    Uri.fromFile(destino)
} catch (e: Exception) {
    null
}

/** Lee una versión reducida de la imagen (una foto de cámara pesa mucho) y la gira si viene acostada. */
fun Context.cargarImagenReducida(uri: Uri, ladoMaximo: Int): Bitmap? = try {
    val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, medidas) }

    if (medidas.outWidth <= 0 || medidas.outHeight <= 0) {
        null
    } else {
        var reduccion = 1
        while (medidas.outWidth / (reduccion * 2) >= ladoMaximo &&
            medidas.outHeight / (reduccion * 2) >= ladoMaximo
        ) reduccion *= 2

        val opciones = BitmapFactory.Options().apply { inSampleSize = reduccion }
        val imagen = contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) }
        imagen?.let { girarSegunExif(uri, it) }
    }
} catch (e: Exception) {
    null
}

private fun Context.girarSegunExif(uri: Uri, imagen: Bitmap): Bitmap {
    val orientacion = contentResolver.openInputStream(uri)?.use {
        ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    } ?: ExifInterface.ORIENTATION_NORMAL

    val grados = when (orientacion) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
    if (grados == 0f) return imagen
    val matriz = Matrix().apply { postRotate(grados) }
    return Bitmap.createBitmap(imagen, 0, 0, imagen.width, imagen.height, matriz, true)
}
/**
 * Id del recurso res/drawable con ese nombre (ej. "museo_oro"), o 0 si no existe.
 * Acepta el nombre con o sin extensión: "museo_oro.jpg" también sirve.
 * Se usa getIdentifier porque el nombre viene de la base de datos y no se conoce al compilar.
 */
@SuppressLint("DiscouragedApi")
@DrawableRes
fun Context.idImagenMuseo(nombre: String): Int {
    val limpio = nombre.trim().substringBeforeLast('.')
    if (limpio.isEmpty()) return 0
    return resources.getIdentifier(limpio, "drawable", packageName)
}


fun esImagenDeArchivo(valor: String): Boolean =
    valor.startsWith("file:") || valor.startsWith("content:")

/** Copia la foto del museo elegida en la galería a la carpeta privada "museos/" */
fun Context.copiarImagenMuseo(uri: Uri): Uri? = try {
    val carpeta = File(filesDir, "museos")
    carpeta.mkdirs()
    val destino = File(carpeta, "museo_${System.currentTimeMillis()}.jpg")
    contentResolver.openInputStream(uri)?.use { entrada ->
        destino.outputStream().use { salida -> entrada.copyTo(salida) }
    }
    Uri.fromFile(destino)
} catch (e: Exception) {
    null
}
