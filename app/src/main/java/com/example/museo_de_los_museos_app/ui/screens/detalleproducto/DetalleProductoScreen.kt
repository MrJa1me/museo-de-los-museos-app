package com.example.museo_de_los_museos_app.ui.screens.detalleproducto

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.model.Product
import com.example.museo_de_los_museos_app.navigation.Routes
import com.example.museo_de_los_museos_app.ui.components.BannerMensaje
import com.example.museo_de_los_museos_app.ui.components.BotonPrincipal
import com.example.museo_de_los_museos_app.ui.screens.catalogo.iconoCategoria
import com.example.museo_de_los_museos_app.ui.screens.catalogo.textoDisponibilidad
import com.example.museo_de_los_museos_app.ui.utils.formatearPrecio
import com.example.museo_de_los_museos_app.ui.utils.texto
import com.example.museo_de_los_museos_app.viewmodel.detalleproducto.DetalleProductoUiState
import com.example.museo_de_los_museos_app.viewmodel.detalleproducto.DetalleProductoViewModel
import com.example.museo_de_los_museos_app.viewmodel.detalleproducto.MensajeDetalleProducto
import kotlinx.coroutines.delay

/** Tiempo que se muestra el mensaje de éxito antes de ocultarse. */
private const val DURACION_MENSAJE_MS = 3000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProductoScreen(
    navController: NavHostController,
    productoId: String,
    viewModel: DetalleProductoViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(productoId) { viewModel.cargar(productoId) }

    // El mensaje de éxito se oculta solo después de unos segundos
    LaunchedEffect(estado.mensaje) {
        if (estado.mensaje != null) {
            delay(DURACION_MENSAJE_MS)
            viewModel.onMensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pantalla_detalle_producto)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.p4_volver))
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.Carrito.route) }) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = stringResource(R.string.catalogo_ir_carrito))
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            val producto = estado.producto
            when {
                estado.cargando -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                estado.noEncontrado || producto == null -> ProductoNoEncontrado(
                    alVolver = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CabeceraProducto(producto)
                    InformacionProducto(producto)

                    when {
                        !producto.disponible -> BannerMensaje(
                            texto = stringResource(R.string.detalle_producto_agotado),
                            esError = true
                        )
                        !estado.haySesion -> AvisoSinSesion(alIniciarSesion = { navController.navigate(Routes.Login.route) })
                        else -> SeccionCompra(
                            estado = estado,
                            producto = producto,
                            viewModel = viewModel
                        )
                    }

                    // Retroalimentación animada al agregar al carrito o solicitar información
                    AnimatedVisibility(
                        visible = estado.mensaje != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            BannerMensaje(
                                texto = stringResource(
                                    if (estado.mensaje == MensajeDetalleProducto.INFORMACION_SOLICITADA) R.string.detalle_info_solicitada
                                    else R.string.detalle_agregado_carrito
                                ),
                                esError = false
                            )
                            if (estado.mensaje == MensajeDetalleProducto.AGREGADO_AL_CARRITO) {
                                TextButton(onClick = { navController.navigate(Routes.Carrito.route) }) {
                                    Text(stringResource(R.string.catalogo_ir_carrito))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CabeceraProducto(producto: Product) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = iconoCategoria(producto.categoria),
                contentDescription = producto.categoria.etiqueta,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(producto.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    producto.categoria.etiqueta,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formatearPrecio(producto.precio),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun InformacionProducto(producto: Product) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (producto.descripcion.isNotBlank()) {
            Text(producto.descripcion, style = MaterialTheme.typography.bodyLarge)
        }
        FilaInfo(
            icono = Icons.Filled.Info,
            texto = textoDisponibilidad(producto),
            esError = !producto.disponible
        )
        val peso = producto.pesoKg
        FilaInfo(
            icono = Icons.Filled.LocalShipping,
            texto = if (peso == null) stringResource(R.string.detalle_sin_envio)
            else stringResource(R.string.detalle_peso, peso.toString().replace('.', ','))
        )
    }
}

@Composable
private fun FilaInfo(icono: ImageVector, texto: String, esError: Boolean = false) {
    val color = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

/** Cantidad con botones − / +, subtotal animado y botones de compra. */
@Composable
private fun SeccionCompra(
    estado: DetalleProductoUiState,
    producto: Product,
    viewModel: DetalleProductoViewModel
) {
    Column(
        modifier = Modifier.animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            stringResource(if (estado.esCurso) R.string.detalle_cantidad_cupos else R.string.detalle_cantidad),
            style = MaterialTheme.typography.titleMedium
        )
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalIconButton(onClick = viewModel::decrementar, modifier = Modifier.padding(top = 8.dp)) {
                Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.detalle_quitar_uno))
            }
            val error = estado.errorCantidad.texto()
            OutlinedTextField(
                value = estado.cantidad,
                onValueChange = viewModel::onCantidadChange,
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.detalle_cantidad)) },
                singleLine = true,
                isError = error != null,
                trailingIcon = {
                    if (error != null) Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                },
                supportingText = { if (error != null) Text(error) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            FilledTonalIconButton(onClick = viewModel::incrementar, modifier = Modifier.padding(top = 8.dp)) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.detalle_agregar_uno))
            }
        }

        if (estado.enCarrito > 0) {
            Text(
                stringResource(R.string.detalle_ya_en_carrito, estado.enCarrito),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // El subtotal cambia con una animación cada vez que cambia la cantidad
        val subtotal = (estado.cantidad.toIntOrNull() ?: 0) * producto.precio
        AnimatedContent(
            targetState = subtotal,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "subtotal"
        ) { valor ->
            Text(
                stringResource(R.string.detalle_subtotal, formatearPrecio(valor)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        BotonPrincipal(
            texto = stringResource(R.string.detalle_agregar_carrito),
            alHacerClick = viewModel::agregarAlCarrito,
            cargando = estado.agregando
        )
        if (estado.esCurso) {
            OutlinedButton(onClick = viewModel::solicitarInformacion, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.detalle_solicitar_info))
            }
        }
    }
}

@Composable
private fun AvisoSinSesion(alIniciarSesion: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.detalle_sin_sesion), textAlign = TextAlign.Center)
            TextButton(onClick = alIniciarSesion) { Text(stringResource(R.string.p4_ir_a_login)) }
        }
    }
}

@Composable
private fun ProductoNoEncontrado(alVolver: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Filled.SearchOff, contentDescription = null, modifier = Modifier.size(64.dp))
        Text(stringResource(R.string.detalle_producto_no_encontrado), textAlign = TextAlign.Center)
        TextButton(onClick = alVolver) { Text(stringResource(R.string.p4_volver)) }
    }
}
