package com.example.museo_de_los_museos_app.ui.screens.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.navigation.Routes
import com.example.museo_de_los_museos_app.ui.components.BannerMensaje
import com.example.museo_de_los_museos_app.ui.components.BotonPrincipal
import com.example.museo_de_los_museos_app.ui.components.CampoTextoValidado
import com.example.museo_de_los_museos_app.ui.utils.texto
import com.example.museo_de_los_museos_app.viewmodel.login.LoginViewModel

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val textoErrorGeneral = estado.errorGeneral.texto()

    // Login correcto: entra al inicio y limpia la pila para que "atrás" no vuelva al login
    LaunchedEffect(estado.loginExitoso) {
        if (estado.loginExitoso) {
            navController.navigate(Routes.Dashboard.route) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.login_titulo),
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = stringResource(R.string.login_subtitulo),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = stringResource(R.string.login_ingresar_como),
            style = MaterialTheme.typography.labelLarge
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Rol.entries.forEach { rol ->
                val seleccionado = estado.rol == rol
                FilterChip(
                    selected = seleccionado,
                    onClick = { viewModel.onRolChange(rol) },
                    label = { Text(rol.etiqueta) },
                    leadingIcon = if (seleccionado) {
                        { Icon(imageVector = Icons.Filled.Done, contentDescription = null) }
                    } else {
                        null
                    }
                )
            }
        }

        CampoTextoValidado(
            valor = estado.correo,
            alCambiar = viewModel::onCorreoChange,
            etiqueta = stringResource(R.string.campo_correo),
            iconoInicial = Icons.Filled.Email,
            mensajeError = estado.errores.correo.texto(),
            tipoTeclado = KeyboardType.Email
        )
        CampoTextoValidado(
            valor = estado.contrasena,
            alCambiar = viewModel::onContrasenaChange,
            etiqueta = stringResource(R.string.campo_contrasena),
            iconoInicial = Icons.Filled.Lock,
            mensajeError = estado.errores.contrasena.texto(),
            esContrasena = true,
            accionIme = ImeAction.Done
        )

        AnimatedVisibility(visible = estado.errorGeneral != null) {
            BannerMensaje(
                texto = textoErrorGeneral.orEmpty(),
                esError = true
            )
        }

        BotonPrincipal(
            texto = stringResource(R.string.login_boton),
            alHacerClick = viewModel::ingresar,
            cargando = estado.cargando
        )

        TextButton(onClick = viewModel::usarCredencialesDemo) {
            Text(stringResource(R.string.login_usar_demo))
        }
        TextButton(onClick = { navController.navigate(Routes.Registro.route) }) {
            Text(stringResource(R.string.login_sin_cuenta))
        }
    }
}
