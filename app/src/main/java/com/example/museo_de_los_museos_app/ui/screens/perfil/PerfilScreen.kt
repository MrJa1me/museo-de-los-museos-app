package com.example.museo_de_los_museos_app.ui.screens.perfil

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.navigation.Routes
import com.example.museo_de_los_museos_app.ui.components.BannerMensaje
import com.example.museo_de_los_museos_app.ui.utils.copiarAvatar
import com.example.museo_de_los_museos_app.ui.utils.crearArchivoAvatar
import com.example.museo_de_los_museos_app.ui.utils.uriCompartible
import com.example.museo_de_los_museos_app.viewmodel.perfil.PerfilViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PerfilScreen(
    navController: NavHostController,
    viewModel: PerfilViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val alcance = rememberCoroutineScope()

    var rutaFotoPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    val lanzadorGaleria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val id = viewModel.userId ?: return@rememberLauncherForActivityResult
        if (uri == null) return@rememberLauncherForActivityResult
        alcance.launch {
            val copia = withContext(Dispatchers.IO) { context.copiarAvatar(uri, id) }
            viewModel.onFotoNueva(copia)
        }
    }

    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val ruta = rutaFotoPendiente
        rutaFotoPendiente = null
        if (exito && ruta != null) viewModel.onFotoNueva(Uri.fromFile(File(ruta)))
    }

    fun abrirCamara() {
        val id = viewModel.userId ?: return
        val archivo = context.crearArchivoAvatar(id)
        rutaFotoPendiente = archivo.absolutePath
        lanzadorCamara.launch(context.uriCompartible(archivo))
    }

    LaunchedEffect(estado.sesionCerrada) {
        if (estado.sesionCerrada) {
            navController.navigate(Routes.Bienvenida.route) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título de Pantalla
        Text(
            text = stringResource(R.string.perfil_titulo),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // 1. Tarjeta Encabezado del Usuario (Avatar + Nombre + Badge Rol + Email)
        EncabezadoUsuario(
            nombre = estado.nombre,
            correo = estado.correo,
            rol = estado.rol,
            fotoUri = estado.fotoUri
        )

        // Banner de mensajes si existen
        AnimatedVisibility(visible = estado.mensaje != null) {
            estado.mensaje?.let { BannerMensaje(texto = stringResource(it), esError = estado.mensajeEsError) }
        }

        // 2. Tarjeta Edición de Datos Personales
        TarjetaEdicionDatos(
            nombre = estado.nombre,
            correo = estado.correo,
            errorNombre = estado.errorNombre,
            guardando = estado.guardando,
            alCambiarNombre = viewModel::onNombreChange,
            alAbrirGaleria = { lanzadorGaleria.launch("image/*") },
            alAbrirCamara = { abrirCamara() },
            alGuardar = viewModel::guardar
        )

        // 3. Sección Configuración en Tarjetas Estilizadas
        estado.preferencias?.let { prefs ->
            SeccionConfiguracion(
                preferencias = prefs,
                rol = estado.rolUsuario,
                alCambiarNotificaciones = viewModel::onNotificacionesChange,
                alCambiarTextoGrande = viewModel::onTextoGrandeChange,
                alCambiarIdioma = viewModel::onIdiomaChange,
                alCambiarOpcionRol = viewModel::onOpcionRolChange
            )
        }

        Spacer(Modifier.height(8.dp))

        // 4. Botón de Cierre de Sesión Estilizado
        BotonCerrarSesionEstilizado(
            alCerrarSesion = viewModel::cerrarSesion
        )
    }
}
