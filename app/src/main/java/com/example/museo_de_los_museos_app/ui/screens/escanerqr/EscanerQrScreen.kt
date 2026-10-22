package com.example.museo_de_los_museos_app.ui.screens.escanerqr

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.model.QrHistory
import com.example.museo_de_los_museos_app.navigation.Routes
import com.example.museo_de_los_museos_app.ui.components.CampoTextoValidado
import com.example.museo_de_los_museos_app.ui.utils.texto
import com.example.museo_de_los_museos_app.viewmodel.escanerqr.EscanerQrViewModel
import com.example.museo_de_los_museos_app.viewmodel.escanerqr.MensajeEscaner
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscanerQrScreen(
    navController: NavHostController,
    viewModel: EscanerQrViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()

    // Recurso nativo: lector QR de Google (Code Scanner). Abre su propia cámara, no necesita el permiso CAMERA.
    val lector = remember {
        GmsBarcodeScanning.getClient(
            context,
            GmsBarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
        )
    }
    val escanear: () -> Unit = {
        lector.startScan()
            .addOnSuccessListener { codigo -> viewModel.onCodigoLeido(codigo.rawValue) }
            .addOnCanceledListener { viewModel.onEscaneoCancelado() }
            .addOnFailureListener { viewModel.onEscaneoFallido() }
    }

    LaunchedEffect(estado.navegarAResultado) {
        if (estado.navegarAResultado) {
            viewModel.onNavegacionRealizada()
            navController.navigate(Routes.ResultadoQr.route)
        }
    }

    val textoMensaje = estado.mensaje?.let {
        stringResource(if (it == MensajeEscaner.CANCELADO) R.string.escaner_cancelado else R.string.escaner_fallo)
    }
    LaunchedEffect(textoMensaje) {
        if (textoMensaje != null) {
            alcance.launch { snackbar.showSnackbar(textoMensaje) }
            viewModel.onMensajeMostrado()
        }
    }

    // Ícono QR que "respira" para invitar a escanear
    val pulso = rememberInfiniteTransition(label = "pulsoQr")
    val escala by pulso.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "escalaQr"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pantalla_escaner_qr)) },
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.QrCodeScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(96.dp).scale(escala)
                        )
                        Text(
                            text = stringResource(R.string.escaner_explicacion),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = escanear, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                            Text(stringResource(R.string.escaner_boton))
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.escaner_manual_titulo), style = MaterialTheme.typography.titleMedium)
                    CampoTextoValidado(
                        valor = estado.textoManual,
                        alCambiar = viewModel::onTextoManualChange,
                        etiqueta = stringResource(R.string.escaner_manual_campo),
                        iconoInicial = Icons.Filled.QrCode,
                        mensajeError = estado.errorManual.texto(),
                        accionIme = ImeAction.Done
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = viewModel::onUsarEjemplo) { Text(stringResource(R.string.escaner_manual_ejemplo)) }
                        OutlinedButton(onClick = viewModel::onRevisarManual) { Text(stringResource(R.string.escaner_manual_boton)) }
                    }
                }
            }

            item {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.escaner_historial), style = MaterialTheme.typography.titleMedium)
            }

            when {
                !estado.haySesion -> item {
                    Text(stringResource(R.string.escaner_historial_sin_sesion), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                estado.historial.isEmpty() -> item {
                    Text(stringResource(R.string.escaner_historial_vacio), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> items(estado.historial, key = { it.contenido }) { escaneo ->
                    ElementoHistorial(
                        escaneo = escaneo,
                        alTocar = { viewModel.onHistorialSeleccionado(escaneo) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@Composable
private fun ElementoHistorial(escaneo: QrHistory, alTocar: () -> Unit, modifier: Modifier = Modifier) {
    val formatoFecha = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }
    ListItem(
        modifier = modifier.clickable(onClick = alTocar),
        leadingContent = {
            Icon(
                imageVector = if (escaneo.valido) Icons.Filled.CheckCircle else Icons.Filled.Error,
                contentDescription = null,
                tint = if (escaneo.valido) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        },
        headlineContent = {
            Text(
                text = escaneo.descripcion.ifBlank { stringResource(R.string.escaner_historial_invalido) },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = { Text(escaneo.contenido, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        trailingContent = { Text(formatoFecha.format(Date(escaneo.fechaMillis)), style = MaterialTheme.typography.labelSmall) }
    )
}
