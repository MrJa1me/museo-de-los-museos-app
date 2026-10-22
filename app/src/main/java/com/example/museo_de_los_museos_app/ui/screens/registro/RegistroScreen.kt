package com.example.museo_de_los_museos_app.ui.screens.registro

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.navigation.Routes
import com.example.museo_de_los_museos_app.ui.components.AvatarInicial
import com.example.museo_de_los_museos_app.ui.components.BannerMensaje
import com.example.museo_de_los_museos_app.ui.components.BotonPrincipal
import com.example.museo_de_los_museos_app.ui.components.CampoTextoValidado
import com.example.museo_de_los_museos_app.ui.utils.texto
import com.example.museo_de_los_museos_app.viewmodel.registro.RegistroViewModel
import kotlinx.coroutines.delay

@Composable
fun RegistroScreen(
    navController: NavHostController,
    viewModel: RegistroViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    // Registro exitoso: muestra el mensaje un momento y vuelve al login
    LaunchedEffect(estado.registroExitoso) {
        if (estado.registroExitoso) {
            delay(1500)
            navController.navigate(Routes.Login.route) {
                popUpTo(Routes.Login.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AvatarInicial(nombre = estado.nombre)

        Text(
            text = stringResource(R.string.registro_titulo),
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = stringResource(R.string.registro_subtitulo),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CampoTextoValidado(
            valor = estado.nombre,
            alCambiar = viewModel::onNombreChange,
            etiqueta = stringResource(R.string.campo_nombre),
            iconoInicial = Icons.Filled.Person,
            mensajeError = estado.errores.nombre.texto(),
            mostrarValido = estado.intentoEnvio && estado.errores.nombre == null
        )
        CampoTextoValidado(
            valor = estado.correo,
            alCambiar = viewModel::onCorreoChange,
            etiqueta = stringResource(R.string.campo_correo),
            iconoInicial = Icons.Filled.Email,
            mensajeError = estado.errores.correo.texto(),
            tipoTeclado = KeyboardType.Email,
            mostrarValido = estado.intentoEnvio && estado.errores.correo == null
        )
        CampoTextoValidado(
            valor = estado.contrasena,
            alCambiar = viewModel::onContrasenaChange,
            etiqueta = stringResource(R.string.campo_contrasena),
            iconoInicial = Icons.Filled.Lock,
            mensajeError = estado.errores.contrasena.texto(),
            esContrasena = true
        )
        CampoTextoValidado(
            valor = estado.confirmacion,
            alCambiar = viewModel::onConfirmacionChange,
            etiqueta = stringResource(R.string.campo_confirmar_contrasena),
            iconoInicial = Icons.Filled.Lock,
            mensajeError = estado.errores.confirmacion.texto(),
            esContrasena = true,
            accionIme = ImeAction.Done
        )

        AnimatedVisibility(visible = estado.registroExitoso) {
            BannerMensaje(
                texto = stringResource(R.string.registro_exitoso),
                esError = false
            )
        }

        BotonPrincipal(
            texto = stringResource(R.string.registro_boton),
            alHacerClick = viewModel::registrar,
            cargando = estado.cargando,
            habilitado = !estado.registroExitoso
        )

        TextButton(onClick = {
            navController.navigate(Routes.Login.route) {
                popUpTo(Routes.Login.route) { inclusive = true }
            }
        }) {
            Text(stringResource(R.string.registro_ya_tengo_cuenta))
        }
    }
}
