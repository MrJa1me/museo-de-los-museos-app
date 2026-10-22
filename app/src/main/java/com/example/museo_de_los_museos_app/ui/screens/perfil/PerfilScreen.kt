package com.example.museo_de_los_museos_app.ui.screens.perfil

import androidx.compose.runtime.mutableStateOf
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.navigation.Routes
import com.example.museo_de_los_museos_app.ui.components.BannerMensaje
import com.example.museo_de_los_museos_app.ui.components.BotonPrincipal
import com.example.museo_de_los_museos_app.ui.components.CampoTextoValidado
import com.example.museo_de_los_museos_app.ui.components.ImagenInteligente
import com.example.museo_de_los_museos_app.ui.utils.copiarAvatar
import com.example.museo_de_los_museos_app.ui.utils.crearArchivoAvatar
import com.example.museo_de_los_museos_app.ui.utils.texto
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

    // Ruta del archivo donde la cámara escribirá la foto. rememberSaveable la conserva si se gira la pantalla.
    var rutaFotoPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    // GALERÍA: GetContent abre el selector de imágenes y devuelve la Uri elegida (o null si se cancela).
    val lanzadorGaleria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val id = viewModel.userId ?: return@rememberLauncherForActivityResult
        if (uri == null) return@rememberLauncherForActivityResult // canceló: no pasa nada
        alcance.launch {
            val copia = withContext(Dispatchers.IO) { context.copiarAvatar(uri, id) }
            viewModel.onFotoNueva(copia)
        }
    }

    // CÁMARA: TakePicture escribe la foto en la Uri que le damos y devuelve true si se tomó.
    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val ruta = rutaFotoPendiente
        rutaFotoPendiente = null
        if (exito && ruta != null) viewModel.onFotoNueva(Uri.fromFile(File(ruta)))
        // si canceló, no se hace nada
    }

    fun abrirCamara() {
        val id = viewModel.userId ?: return
        val archivo = context.crearArchivoAvatar(id)
        rutaFotoPendiente = archivo.absolutePath
        lanzadorCamara.launch(context.uriCompartible(archivo))
    }

    // Al cerrar sesión se vuelve a Bienvenida y se borra el historial de navegación.
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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.perfil_titulo), style = MaterialTheme.typography.headlineMedium)

        ImagenInteligente(uri = estado.fotoUri, descripcion = stringResource(R.string.perfil_cd_foto))

        Text(
            text = stringResource(R.string.perfil_rol, estado.rol),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { lanzadorGaleria.launch("image/*") }) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.perfil_galeria))
            }
            OutlinedButton(onClick = { abrirCamara() }) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.perfil_camara))
            }
        }

        CampoTextoValidado(
            valor = estado.nombre,
            alCambiar = viewModel::onNombreChange,
            etiqueta = stringResource(R.string.perfil_nombre),
            iconoInicial = Icons.Filled.Person,
            mensajeError = estado.errorNombre.texto(),
            accionIme = ImeAction.Done
        )

        // El correo identifica la cuenta: se muestra, pero no se edita.
        OutlinedTextField(
            value = estado.correo,
            onValueChange = {},
            label = { Text(stringResource(R.string.perfil_correo)) },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
            enabled = false,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(visible = estado.mensaje != null) {
            estado.mensaje?.let { BannerMensaje(texto = stringResource(it), esError = estado.mensajeEsError) }
        }

        BotonPrincipal(
            texto = stringResource(R.string.perfil_guardar),
            alHacerClick = viewModel::guardar,
            cargando = estado.guardando
        )

        TextButton(onClick = viewModel::cerrarSesion) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.perfil_cerrar_sesion))
        }
    }
}