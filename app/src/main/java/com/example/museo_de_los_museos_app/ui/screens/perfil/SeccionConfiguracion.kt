package com.example.museo_de_los_museos_app.ui.screens.perfil

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.model.Preferencias
import com.example.museo_de_los_museos_app.model.Rol

/** Sección "Configuración" de Perfil rediseñada con tarjetas estilizadas. */
@Composable
fun SeccionConfiguracion(
    preferencias: Preferencias,
    rol: Rol?,
    alCambiarNotificaciones: (Boolean) -> Unit,
    alCambiarTextoGrande: (Boolean) -> Unit,
    alCambiarIdioma: (String) -> Unit,
    alCambiarOpcionRol: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.config_titulo),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Tarjeta de Ajustes Generales
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                ItemOpcionEstilizado(
                    icono = Icons.Filled.Notifications,
                    titulo = stringResource(R.string.config_notificaciones),
                    controlDerecha = {
                        Switch(
                            checked = preferencias.notificaciones,
                            onCheckedChange = alCambiarNotificaciones
                        )
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                ItemOpcionEstilizado(
                    icono = Icons.Filled.TextFields,
                    titulo = stringResource(R.string.config_texto_grande),
                    controlDerecha = {
                        Switch(
                            checked = preferencias.textoGrande,
                            onCheckedChange = alCambiarTextoGrande
                        )
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Selector de Idioma estilizado
                var expandirIdioma by remember { mutableStateOf(false) }
                Column(modifier = Modifier.fillMaxWidth()) {
                    ItemOpcionEstilizado(
                        icono = Icons.Filled.Language,
                        titulo = stringResource(R.string.config_idioma),
                        subtitulo = if (preferencias.idioma == "es") stringResource(R.string.config_idioma_es) else stringResource(R.string.config_idioma_en),
                        alHacerClick = { expandirIdioma = !expandirIdioma },
                        controlDerecha = {
                            Icon(
                                imageVector = if (expandirIdioma) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )

                    AnimatedVisibility(visible = expandirIdioma) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = preferencias.idioma == "es",
                                    onClick = { alCambiarIdioma("es") }
                                )
                                Text(stringResource(R.string.config_idioma_es))
                                Spacer(Modifier.width(16.dp))
                                RadioButton(
                                    selected = preferencias.idioma == "en",
                                    onClick = { alCambiarIdioma("en") }
                                )
                                Text(stringResource(R.string.config_idioma_en))
                            }
                            Text(
                                text = stringResource(R.string.config_idioma_nota),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Tarjeta de Permisos de Sistema
        SeccionPermisosEstilizada()

        // Tarjeta con la Opción propia del Rol
        val textoRol = when (rol) {
            Rol.ADMINISTRADOR -> R.string.config_rol_admin
            Rol.INVESTIGADOR -> R.string.config_rol_investigador
            Rol.CLIENTE -> R.string.config_rol_cliente
            null -> null
        }
        if (rol != null && textoRol != null) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                ItemOpcionEstilizado(
                    icono = Icons.Filled.Star,
                    titulo = stringResource(R.string.config_opciones_rol, rol.etiqueta),
                    subtitulo = stringResource(textoRol),
                    controlDerecha = {
                        Switch(
                            checked = preferencias.opcionRol,
                            onCheckedChange = alCambiarOpcionRol
                        )
                    }
                )
            }
        }
    }
}

/** Estado de los permisos formateado en una tarjeta estilizada. */
@Composable
private fun SeccionPermisosEstilizada() {
    val context = LocalContext.current

    fun tieneUbicacion(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    var ubicacionConcedida by remember { mutableStateOf(tieneUbicacion()) }

    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado -> ubicacionConcedida = resultado.values.any { it } }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ItemOpcionEstilizado(
                icono = Icons.Filled.LocationOn,
                titulo = stringResource(R.string.config_permisos) + " (Ubicación)",
                subtitulo = stringResource(if (ubicacionConcedida) R.string.config_ubicacion_si else R.string.config_ubicacion_no),
                controlDerecha = {
                    if (!ubicacionConcedida) {
                        TextButton(onClick = {
                            lanzadorPermiso.launch(
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                            )
                        }) {
                            Text(stringResource(R.string.config_permitir))
                        }
                    }
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            ItemOpcionEstilizado(
                icono = Icons.Filled.PhotoCamera,
                titulo = "Cámara del Sistema",
                subtitulo = stringResource(R.string.config_camara)
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.config_abrir_ajustes))
                }
            }
        }
    }
}
