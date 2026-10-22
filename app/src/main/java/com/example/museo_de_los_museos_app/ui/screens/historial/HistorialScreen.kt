package com.example.museo_de_los_museos_app.ui.screens.historial

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.model.EstadoPedido
import com.example.museo_de_los_museos_app.model.Order
import com.example.museo_de_los_museos_app.navigation.Routes
import com.example.museo_de_los_museos_app.ui.components.BannerMensaje
import com.example.museo_de_los_museos_app.ui.utils.formatearPrecio
import com.example.museo_de_los_museos_app.viewmodel.historial.HistorialViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Tiempo que se muestra el mensaje de cambio de estado antes de ocultarse. */
private const val DURACION_MENSAJE_MS = 3000L

private val formatoFecha = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.forLanguageTag("es-CL"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    navController: NavHostController,
    viewModel: HistorialViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(estado.ultimoCambio) {
        if (estado.ultimoCambio != null) {
            delay(DURACION_MENSAJE_MS)
            viewModel.onMensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (estado.esAdministrador) R.string.historial_titulo_admin else R.string.pantalla_historial
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.p4_volver))
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                estado.cargando -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                !estado.haySesion -> EstadoCentral(
                    icono = Icons.Filled.Lock,
                    texto = stringResource(R.string.historial_sin_sesion),
                    textoBoton = stringResource(R.string.p4_ir_a_login),
                    alHacerClick = { navController.navigate(Routes.Login.route) },
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> Column(modifier = Modifier.fillMaxSize()) {
                    FiltrosEstado(seleccionado = estado.filtroEstado, alElegir = viewModel::onFiltroChange)

                    // Confirmación animada cuando el Administrador cambia un estado
                    AnimatedVisibility(
                        visible = estado.ultimoCambio != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        val cambio = estado.ultimoCambio
                        if (cambio != null) {
                            BannerMensaje(
                                texto = stringResource(
                                    R.string.historial_estado_actualizado,
                                    HistorialViewModel.codigoCorto(cambio.pedidoId),
                                    cambio.nuevoEstado.etiqueta
                                ),
                                esError = false,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    if (estado.pedidos.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            EstadoCentral(
                                icono = Icons.AutoMirrored.Filled.ReceiptLong,
                                texto = stringResource(
                                    if (estado.filtroEstado != null) R.string.historial_vacio_filtro else R.string.historial_vacio
                                ),
                                textoBoton = stringResource(R.string.carrito_ir_catalogo),
                                alHacerClick = { navController.navigate(Routes.Catalogo.route) { launchSingleTop = true } },
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    stringResource(R.string.historial_contador, estado.pedidos.size),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            items(estado.pedidos, key = { it.id }) { pedido ->
                                TarjetaPedido(
                                    pedido = pedido,
                                    nombreCliente = if (estado.esAdministrador) estado.nombresClientes[pedido.userId] ?: pedido.userId else null,
                                    esAdministrador = estado.esAdministrador,
                                    alAvanzar = { viewModel.avanzarEstado(pedido.id) },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltrosEstado(seleccionado: EstadoPedido?, alElegir: (EstadoPedido?) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        FilterChip(
            selected = seleccionado == null,
            onClick = { alElegir(null) },
            label = { Text(stringResource(R.string.catalogo_todas)) }
        )
        EstadoPedido.entries.forEach { estado ->
            FilterChip(
                selected = seleccionado == estado,
                onClick = { alElegir(estado) },
                label = { Text(estado.etiqueta) }
            )
        }
    }
}

@Composable
private fun TarjetaPedido(
    pedido: Order,
    nombreCliente: String?,
    esAdministrador: Boolean,
    alAvanzar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandido by rememberSaveable(pedido.id) { mutableStateOf(false) }
    // La barra avanza con animación cuando cambia el estado (Pendiente 25% … Entregado 100%)
    val progreso by animateFloatAsState(
        targetValue = (pedido.estado.ordinal + 1f) / EstadoPedido.entries.size,
        label = "progresoEstado"
    )

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp).animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.historial_solicitud, HistorialViewModel.codigoCorto(pedido.id)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        formatoFecha.format(Date(pedido.fechaMillis)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (nombreCliente != null) {
                        Text(stringResource(R.string.historial_cliente, nombreCliente), style = MaterialTheme.typography.bodySmall)
                    }
                }
                EtiquetaEstado(pedido.estado)
            }

            LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth())

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.historial_resumen_items, pedido.items.sumOf { it.cantidad }, formatearPrecio(pedido.total)),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { expandido = !expandido }) {
                    Icon(
                        if (expandido) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = stringResource(if (expandido) R.string.historial_ocultar else R.string.historial_ver_detalle)
                    )
                }
            }

            if (expandido) {
                DetallePedido(pedido)
            }

            val siguiente = pedido.estado.siguiente()
            if (esAdministrador && siguiente != null) {
                Button(onClick = alAvanzar, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.historial_avanzar, siguiente.etiqueta))
                }
            }
        }
    }
}

@Composable
private fun DetallePedido(pedido: Order) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        HorizontalDivider()
        pedido.items.forEach { item ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("${item.cantidad} × ${item.nombre}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                Text(formatearPrecio(item.precioUnitario * item.cantidad), style = MaterialTheme.typography.bodySmall)
            }
        }
        HorizontalDivider()
        FilaDetalle(stringResource(R.string.carrito_subtotal), formatearPrecio(pedido.subtotal))
        FilaDetalle(stringResource(R.string.carrito_envio), formatearPrecio(pedido.costoEnvio))
        FilaDetalle(stringResource(R.string.carrito_total), formatearPrecio(pedido.total))
        if (pedido.destino.isNotBlank()) {
            FilaDetalle(stringResource(R.string.historial_destino), pedido.destino)
        }
        if (pedido.modalidad.isNotBlank()) {
            FilaDetalle(stringResource(R.string.carrito_modalidad), pedido.modalidad)
        }
    }
}

@Composable
private fun FilaDetalle(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(etiqueta, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}

/** Etiqueta del estado con color animado: cambia suavemente al avanzar de estado. */
@Composable
private fun EtiquetaEstado(estado: EstadoPedido) {
    val colorObjetivo = when (estado) {
        EstadoPedido.PENDIENTE -> Color(0xFFF9A825)
        EstadoPedido.CONFIRMADO -> Color(0xFF1E88E5)
        EstadoPedido.ENVIADO -> Color(0xFF8E24AA)
        EstadoPedido.ENTREGADO -> Color(0xFF43A047)
    }
    val color by animateColorAsState(targetValue = colorObjetivo, label = "colorEstado")
    Surface(color = color, contentColor = Color.White, shape = MaterialTheme.shapes.small) {
        Text(
            estado.etiqueta,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/** Estado vacío o sin sesión, con animación de entrada (escala + fundido). */
@Composable
private fun EstadoCentral(
    icono: ImageVector,
    texto: String,
    textoBoton: String,
    alHacerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aparicion = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(visibleState = aparicion, enter = scaleIn() + fadeIn(), modifier = modifier) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
            Text(texto, textAlign = TextAlign.Center)
            TextButton(onClick = alHacerClick) { Text(textoBoton) }
        }
    }
}
