package com.example.museo_de_los_museos_app.ui.screens.perfil

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.model.Preferencias
import com.example.museo_de_los_museos_app.model.Rol

/** Sección "Configuración" de Perfil. Solo dibuja: los cambios se los avisa al ViewModel. */
@Composable
fun SeccionConfiguracion(
    preferencias: Preferencias,
    rol: Rol?,
    alCambiarNotificaciones: (Boolean) -> Unit,
    alCambiarTextoGrande: (Boolean) -> Unit,
    alCambiarIdioma: (String) -> Unit,
    alCambiarOpcionRol: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.config_titulo), style = MaterialTheme.typography.titleLarge)

        FilaSwitch(
            Icons.Filled.Notifications, R.string.config_notificaciones,
            preferencias.notificaciones, alCambiarNotificaciones
        )
        FilaSwitch(
            Icons.Filled.TextFields, R.string.config_texto_grande,
            preferencias.textoGrande, alCambiarTextoGrande
        )

        // Idioma: dos botones de opción
        Text(stringResource(R.string.config_idioma), style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = preferencias.idioma == "es", onClick = { alCambiarIdioma("es") })
            Text(stringResource(R.string.config_idioma_es))
            Spacer(Modifier.width(16.dp))
            RadioButton(selected = preferencias.idioma == "en", onClick = { alCambiarIdioma("en") })
            Text(stringResource(R.string.config_idioma_en))
        }
        Text(stringResource(R.string.config_idioma_nota), style = MaterialTheme.typography.bodySmall)

        SeccionPermisos()

        // Opción distinta según el rol
        val textoRol = when (rol) {
            Rol.ADMINISTRADOR -> R.string.config_rol_admin
            Rol.INVESTIGADOR -> R.string.config_rol_investigador
            Rol.CLIENTE -> R.string.config_rol_cliente
            null -> null
        }
        if (rol != null && textoRol != null) {
            Text(
                stringResource(R.string.config_opciones_rol, rol.etiqueta),
                style = MaterialTheme.typography.titleMedium
            )
            FilaSwitch(Icons.Filled.Star, textoRol, preferencias.opcionRol, alCambiarOpcionRol)
        }
    }
}

/** Una fila: ícono + texto + switch a la derecha. */
@Composable
private fun FilaSwitch(icono: ImageVector, @StringRes texto: Int, marcado: Boolean, alCambiar: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null)
        Spacer(Modifier.width(12.dp))
        Text(stringResource(texto), modifier = Modifier.weight(1f))
        Switch(checked = marcado, onCheckedChange = alCambiar)
    }
}

/** Estado de los permisos y botones para pedirlos o ir a los ajustes de Android. */
@Composable
private fun SeccionPermisos() {
    val context = LocalContext.current

    fun tieneUbicacion(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    var ubicacionConcedida by remember { mutableStateOf(tieneUbicacion()) }

    // Muestra el diálogo del sistema "¿Permitir que la app acceda a tu ubicación?"
    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado -> ubicacionConcedida = resultado.values.any { it } }

    Text(stringResource(R.string.config_permisos), style = MaterialTheme.typography.titleMedium)

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.LocationOn, contentDescription = null)
        Spacer(Modifier.width(12.dp))
        Text(
            stringResource(if (ubicacionConcedida) R.string.config_ubicacion_si else R.string.config_ubicacion_no),
            modifier = Modifier.weight(1f)
        )
        if (!ubicacionConcedida) {
            TextButton(onClick = {
                lanzadorPermiso.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            }) { Text(stringResource(R.string.config_permitir)) }
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.PhotoCamera, contentDescription = null)
        Spacer(Modifier.width(12.dp))
        Text(stringResource(R.string.config_camara), style = MaterialTheme.typography.bodyMedium)
    }

    // Si el usuario negó el permiso dos veces, Android ya no muestra el diálogo: solo queda ir a Ajustes.
    OutlinedButton(onClick = {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        context.startActivity(intent)
    }) { Text(stringResource(R.string.config_abrir_ajustes)) }
}
