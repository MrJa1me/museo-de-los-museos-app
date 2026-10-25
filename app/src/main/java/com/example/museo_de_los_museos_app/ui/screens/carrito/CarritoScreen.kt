package com.example.museo_de_los_museos_app.ui.screens.carrito

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.domain.LineaCarrito
import com.example.museo_de_los_museos_app.domain.ModalidadEnvio
import com.example.museo_de_los_museos_app.domain.RegionesChile
import com.example.museo_de_los_museos_app.domain.ZonaInternacional
import com.example.museo_de_los_museos_app.navigation.Routes
import com.example.museo_de_los_museos_app.ui.components.BannerMensaje
import com.example.museo_de_los_museos_app.ui.components.BotonPrincipal
import com.example.museo_de_los_museos_app.ui.components.CampoTextoValidado
import com.example.museo_de_los_museos_app.ui.screens.catalogo.iconoCategoria
import com.example.museo_de_los_museos_app.ui.utils.formatearPrecio
import com.example.museo_de_los_museos_app.ui.utils.texto
import com.example.museo_de_los_museos_app.viewmodel.carrito.CarritoUiState
import com.example.museo_de_los_museos_app.viewmodel.carrito.CarritoViewModel
import com.example.museo_de_los_museos_app.viewmodel.carrito.TipoEnvio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(
    navController: NavHostController,
    viewModel: CarritoViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pantalla_carrito)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.p4_volver))
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.Historial.route) }) {
                        Icon(Icons.Filled.History, contentDescription = stringResource(R.string.catalogo_ir_historial))
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            val pedidoConfirmado = estado.pedidoConfirmado
            when {
                estado.cargando -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                !estado.haySesion -> EstadoCentral(
                    icono = Icons.Filled.Lock,
                    texto = stringResource(R.string.carrito_sin_sesion),
                    textoBoton = stringResource(R.string.p4_ir_a_login),
                    alHacerClick = { navController.navigate(Routes.Login.route) },
                    modifier = Modifier.align(Alignment.Center)
                )

                pedidoConfirmado != null -> EstadoCentral(
                    icono = Icons.Filled.CheckCircle,
                    texto = stringResource(R.string.carrito_pedido_confirmado, pedidoConfirmado),
                    textoBoton = stringResource(R.string.carrito_ver_historial),
                    alHacerClick = {
                        viewModel.onPedidoConfirmadoVisto()
                        navController.navigate(Routes.Historial.route)
                    },
                    modifier = Modifier.align(Alignment.Center)
                )

                estado.vacio -> EstadoCentral(
                    icono = Icons.Filled.RemoveShoppingCart,
                    texto = stringResource(R.string.carrito_vacio),
                    textoBoton = stringResource(R.string.carrito_ir_catalogo),
                    alHacerClick = { navController.navigate(Routes.Catalogo.route) { launchSingleTop = true } },
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> ContenidoCarrito(estado = estado, viewModel = viewModel)
            }
        }
    }

    if (estado.mostrandoConfirmacion) {
        DialogoConfirmacion(estado = estado, viewModel = viewModel)
    }
}

@Composable
private fun ContenidoCarrito(estado: CarritoUiState, viewModel: CarritoViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // animateItem: al quitar un producto, el resto se desliza a su nuevo lugar
        items(estado.lineas, key = { it.producto.id }) { linea ->
            TarjetaLinea(
                linea = linea,
                disponible = linea.producto.activo,
                alSumar = { viewModel.incrementar(linea.producto.id) },
                alRestar = { viewModel.decrementar(linea.producto.id) },
                alEliminar = { viewModel.eliminar(linea.producto.id) },
                modifier = Modifier.animateItem()
            )
        }

        if (estado.hayNoDisponibles) {
            item {
                BannerMensaje(
                    texto = stringResource(R.string.carrito_hay_no_disponibles),
                    esError = true
                )
            }
        }

        item {
            AnimatedVisibility(
                visible = estado.productoSinStock != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                BannerMensaje(
                    texto = stringResource(R.string.carrito_sin_stock, estado.productoSinStock.orEmpty()),
                    esError = true
                )
            }
        }

        if (estado.requiereEnvio) {
            item { FormularioEnvio(estado = estado, viewModel = viewModel) }
        } else if (estado.lineasDisponibles.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.carrito_solo_cursos),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item { ResumenTotales(estado) }

        item {
            BotonPrincipal(
                texto = stringResource(R.string.carrito_confirmar),
                alHacerClick = viewModel::solicitarConfirmacion,
                cargando = estado.confirmando,
                habilitado = !estado.hayNoDisponibles
            )
        }
    }
}

@Composable
private fun TarjetaLinea(
    linea: LineaCarrito,
    disponible: Boolean,
    alSumar: () -> Unit,
    alRestar: () -> Unit,
    alEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                iconoCategoria(linea.producto.categoria),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(linea.producto.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(R.string.carrito_precio_unitario, formatearPrecio(linea.producto.precio)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (disponible) {
                    Text(
                        formatearPrecio(linea.producto.precio * linea.cantidad),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        stringResource(R.string.catalogo_no_disponible),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            IconButton(onClick = alRestar, enabled = disponible) {
                Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.detalle_quitar_uno))
            }
            AnimatedContent(targetState = linea.cantidad, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "cantidad") {
                Text(it.toString(), style = MaterialTheme.typography.titleMedium)
            }
            IconButton(onClick = alSumar, enabled = disponible) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.detalle_agregar_uno))
            }
            IconButton(onClick = alEliminar) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.carrito_eliminar),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/** Formulario de destino: tipo de envío, datos de entrega, región o país, y modalidad. */
@Composable
private fun FormularioEnvio(estado: CarritoUiState, viewModel: CarritoViewModel) {
    val f = estado.formulario
    val e = estado.errores
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp).animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(stringResource(R.string.carrito_destino_titulo), style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = f.tipo == TipoEnvio.NACIONAL,
                    onClick = { viewModel.onTipoEnvioChange(TipoEnvio.NACIONAL) },
                    label = { Text(stringResource(R.string.carrito_nacional)) }
                )
                FilterChip(
                    selected = f.tipo == TipoEnvio.INTERNACIONAL,
                    onClick = { viewModel.onTipoEnvioChange(TipoEnvio.INTERNACIONAL) },
                    label = { Text(stringResource(R.string.carrito_internacional)) }
                )
            }

            CampoTextoValidado(
                valor = f.nombre,
                alCambiar = viewModel::onNombreChange,
                etiqueta = stringResource(R.string.carrito_campo_nombre),
                iconoInicial = Icons.Filled.Person,
                mensajeError = e.nombre.texto()
            )
            CampoTextoValidado(
                valor = f.direccion,
                alCambiar = viewModel::onDireccionChange,
                etiqueta = stringResource(R.string.carrito_campo_direccion),
                iconoInicial = Icons.Filled.Home,
                mensajeError = e.direccion.texto()
            )
            CampoTextoValidado(
                valor = f.ciudad,
                alCambiar = viewModel::onCiudadChange,
                etiqueta = stringResource(R.string.carrito_campo_ciudad),
                iconoInicial = Icons.Filled.LocationCity,
                mensajeError = e.ciudad.texto()
            )
            CampoTextoValidado(
                valor = f.telefono,
                alCambiar = viewModel::onTelefonoChange,
                etiqueta = stringResource(R.string.carrito_campo_telefono),
                iconoInicial = Icons.Filled.Phone,
                mensajeError = e.telefono.texto(),
                tipoTeclado = KeyboardType.Phone
            )

            if (f.tipo == TipoEnvio.NACIONAL) {
                Selector(
                    etiqueta = stringResource(R.string.carrito_campo_region),
                    valor = f.region,
                    opciones = RegionesChile.todas,
                    alElegir = viewModel::onRegionChange,
                    mensajeError = e.region.texto()
                )
            } else {
                Selector(
                    etiqueta = stringResource(R.string.carrito_campo_pais),
                    valor = f.pais,
                    opciones = ZonaInternacional.paisesDisponibles,
                    alElegir = viewModel::onPaisChange,
                    mensajeError = e.pais.texto()
                )
            }

            Text(stringResource(R.string.carrito_modalidad), style = MaterialTheme.typography.titleSmall)
            ModalidadEnvio.entries.forEach { modalidad ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = f.modalidad == modalidad,
                            onClick = { viewModel.onModalidadChange(modalidad) },
                            role = Role.RadioButton
                        )
                ) {
                    RadioButton(selected = f.modalidad == modalidad, onClick = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(
                            if (modalidad == ModalidadEnvio.EXPRESS) R.string.carrito_modalidad_express
                            else R.string.carrito_modalidad_estandar
                        )
                    )
                }
            }
        }
    }
}

/** Campo de solo lectura que abre una lista para elegir (región o país). */
@Composable
private fun Selector(
    etiqueta: String,
    valor: String,
    opciones: List<String>,
    alElegir: (String) -> Unit,
    mensajeError: String?
) {
    var abierto by rememberSaveable { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            isError = mensajeError != null,
            supportingText = { if (mensajeError != null) Text(mensajeError) },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        // Capa transparente encima del campo para abrir la lista al tocarlo
        Box(modifier = Modifier.matchParentSize().clickable { abierto = true })
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        alElegir(opcion)
                        abierto = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ResumenTotales(estado: CarritoUiState) {
    val resultado = estado.resultado
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp).animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(stringResource(R.string.carrito_resumen), style = MaterialTheme.typography.titleMedium)
            FilaResumen(stringResource(R.string.carrito_subtotal), formatearPrecio(estado.subtotal))
            if (resultado != null && resultado.requiereEnvio) {
                FilaResumen(
                    stringResource(R.string.carrito_peso_total),
                    stringResource(R.string.detalle_peso, resultado.pesoTotalKg.toString().replace('.', ','))
                )
            }
            FilaResumen(
                stringResource(R.string.carrito_envio),
                when {
                    resultado == null -> stringResource(R.string.carrito_envio_pendiente)
                    !resultado.requiereEnvio -> stringResource(R.string.carrito_envio_no_aplica)
                    resultado.envioGratis -> stringResource(R.string.carrito_envio_gratis)
                    else -> formatearPrecio(resultado.costoEnvio)
                }
            )
            HorizontalDivider()
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.carrito_total),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                // El total cambia con animación al cambiar cantidades, destino o modalidad
                AnimatedContent(
                    targetState = resultado?.total,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "total"
                ) { total ->
                    Text(
                        if (total == null) "—" else formatearPrecio(total),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun FilaResumen(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(etiqueta, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor)
    }
}

/** Muestra el total final antes de confirmar la solicitud (pedido del DOCX). */
@Composable
private fun DialogoConfirmacion(estado: CarritoUiState, viewModel: CarritoViewModel) {
    val resultado = estado.resultado ?: return
    AlertDialog(
        onDismissRequest = viewModel::cancelarConfirmacion,
        title = { Text(stringResource(R.string.carrito_dialogo_titulo)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FilaResumen(stringResource(R.string.carrito_subtotal), formatearPrecio(resultado.subtotal))
                FilaResumen(
                    stringResource(R.string.carrito_envio),
                    if (resultado.envioGratis || !resultado.requiereEnvio) formatearPrecio(0) else formatearPrecio(resultado.costoEnvio)
                )
                HorizontalDivider()
                FilaResumen(stringResource(R.string.carrito_total), formatearPrecio(resultado.total))
            }
        },
        confirmButton = {
            Button(onClick = viewModel::confirmarPedido) { Text(stringResource(R.string.carrito_dialogo_confirmar)) }
        },
        dismissButton = {
            OutlinedButton(onClick = viewModel::cancelarConfirmacion) { Text(stringResource(R.string.carrito_dialogo_cancelar)) }
        }
    )
}

/** Estado vacío, sin sesión o pedido confirmado, con animación de entrada (escala + fundido). */
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
