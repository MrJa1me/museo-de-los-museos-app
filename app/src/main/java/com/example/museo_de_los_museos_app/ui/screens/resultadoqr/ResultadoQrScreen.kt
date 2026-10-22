package com.example.museo_de_los_museos_app.ui.screens.resultadoqr

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.domain.ResultadoQr
import com.example.museo_de_los_museos_app.navigation.Routes
import com.example.museo_de_los_museos_app.ui.utils.formatearPrecio
import com.example.museo_de_los_museos_app.ui.utils.texto
import com.example.museo_de_los_museos_app.viewmodel.resultadoqr.ResultadoQrViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultadoQrScreen(
    navController: NavHostController,
    viewModel: ResultadoQrViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val textoErrorEnlace = stringResource(R.string.detalle_recorrido_error)

    val escanearOtro: () -> Unit = {
        if (!navController.popBackStack(Routes.EscanerQr.route, inclusive = false)) {
            navController.navigate(Routes.EscanerQr.route)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pantalla_resultado_qr)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.p3_volver))
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val resultado = estado.resultado
            if (resultado == null) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = null, modifier = Modifier.size(72.dp))
                Text(stringResource(R.string.resultado_vacio))
            } else {
                IconoAnimado(esExito = estado.esExito, clave = estado.contenido)
                Text(
                    text = stringResource(if (estado.esExito) R.string.resultado_exito else R.string.resultado_error),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // La tarjeta entra desde abajo después del ícono
                val entradaTarjeta = remember(estado.contenido) { MutableTransitionState(false).apply { targetState = true } }
                AnimatedVisibility(
                    visibleState = entradaTarjeta,
                    enter = slideInVertically(tween(400, delayMillis = 150)) { it / 2 } + fadeIn(tween(400, delayMillis = 150))
                ) {
                    TarjetaResultado(
                        resultado = resultado,
                        contenido = estado.contenido,
                        alVerMuseo = { id -> navController.navigate(Routes.DetalleMuseo.crear(id)) },
                        alVerProducto = { id -> navController.navigate(Routes.DetalleProducto.crear(id)) },
                        alAbrirEnlace = { url ->
                            runCatching { uriHandler.openUri(url) }
                                .onFailure { alcance.launch { snackbar.showSnackbar(textoErrorEnlace) } }
                        }
                    )
                }
            }

            Button(onClick = escanearOtro, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(stringResource(R.string.resultado_escanear_otro))
            }
            TextButton(onClick = {
                navController.navigate(Routes.Mapa.route) {
                    popUpTo(Routes.Dashboard.route)
                    launchSingleTop = true
                }
            }) {
                Text(stringResource(R.string.resultado_volver_mapa))
            }
        }
    }
}

/** Éxito: el check aparece con un rebote. Error: el ícono aparece y se sacude de lado a lado. */
@Composable
private fun IconoAnimado(esExito: Boolean, clave: String) {
    val aparicion = remember(clave) { MutableTransitionState(false).apply { targetState = true } }
    val sacudida = remember(clave) { Animatable(0f) }

    LaunchedEffect(clave, esExito) {
        if (!esExito) {
            repeat(3) {
                sacudida.animateTo(16f, tween(60))
                sacudida.animateTo(-16f, tween(60))
            }
            sacudida.animateTo(0f, tween(60))
        }
    }

    AnimatedVisibility(
        visibleState = aparicion,
        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()
    ) {
        Icon(
            imageVector = if (esExito) Icons.Filled.CheckCircle else Icons.Filled.Error,
            contentDescription = null,
            tint = if (esExito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier
                .size(112.dp)
                .graphicsLayer { translationX = sacudida.value }
        )
    }
}

@Composable
private fun TarjetaResultado(
    resultado: ResultadoQr,
    contenido: String,
    alVerMuseo: (String) -> Unit,
    alVerProducto: (String) -> Unit,
    alAbrirEnlace: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when (resultado) {
                is ResultadoQr.MuseoEncontrado -> {
                    Etiqueta(stringResource(R.string.resultado_museo))
                    Text(resultado.museo.nombre, style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.detalle_ubicacion, resultado.museo.ciudad, resultado.museo.pais))
                    Button(onClick = { alVerMuseo(resultado.museo.id) }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.resultado_ver_museo))
                    }
                }
                is ResultadoQr.ProductoEncontrado -> {
                    Etiqueta(stringResource(R.string.resultado_producto))
                    Text(resultado.producto.nombre, style = MaterialTheme.typography.titleLarge)
                    Text(resultado.producto.categoria.etiqueta + " · " + formatearPrecio(resultado.producto.precio))
                    Button(onClick = { alVerProducto(resultado.producto.id) }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.resultado_ver_producto))
                    }
                }
                is ResultadoQr.EnlaceValido -> {
                    Etiqueta(stringResource(R.string.resultado_enlace))
                    Text(resultado.url, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    OutlinedButton(onClick = { alAbrirEnlace(resultado.url) }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.resultado_abrir_enlace))
                    }
                }
                is ResultadoQr.Error -> {
                    Text(
                        text = resultado.motivo.texto().orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (contenido.isNotBlank()) {
                        Text(
                            text = contenido,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Etiqueta(texto: String) {
    Text(texto, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
}
