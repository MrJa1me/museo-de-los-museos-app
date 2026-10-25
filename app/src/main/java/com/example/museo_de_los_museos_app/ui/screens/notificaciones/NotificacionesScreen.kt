package com.example.museo_de_los_museos_app.ui.screens.notificaciones

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.model.NotificationItem
import com.example.museo_de_los_museos_app.viewmodel.notificaciones.FiltroNotificacion
import com.example.museo_de_los_museos_app.viewmodel.notificaciones.NotificacionesUiState
import com.example.museo_de_los_museos_app.viewmodel.notificaciones.NotificacionesViewModel
import com.example.museo_de_los_museos_app.viewmodel.notificaciones.OpcionDestinatario
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.museo_de_los_museos_app.navigation.navegarDesdeAviso

/**
 * Pantalla adaptativa de Notificaciones y Avisos de la app (P2).
 * Soporta dos paneles en pantallas anchas, animaciones fluidas, selector de destinatarios para Admin
 * y retroalimentación visual completa de errores.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    navController: NavHostController,
    viewModel: NotificacionesViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val esPantallaAncha = configuration.screenWidthDp >= 600

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BadgedBox(
                            badge = {
                                if (uiState.noLeidasCount > 0) {
                                    Badge { Text(uiState.noLeidasCount.toString()) }
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = null,
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.notif_titulo),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (uiState.esAdmin && uiState.tieneSesionActiva) {
                FloatingActionButton(
                    onClick = { viewModel.setMostrarDialogoCrear(mostrar = true) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.notif_crear_aviso))
                    }
                }
            }
        },
    ) { paddingValues ->
        if (!uiState.tieneSesionActiva) {
            // Sin sesión activa
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.notif_sin_sesion),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else if (esPantallaAncha) {
            // Vista de dos paneles para pantalla ancha (Guía 9)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BarraFiltros(uiState = uiState, onFiltroChanged = viewModel::cambiarFiltro)
                    ListaNotificaciones(
                        uiState = uiState,
                        onSeleccionar = viewModel::seleccionarNotificacion,
                        onMarcarLeida = viewModel::marcarComoLeida,
                        onNavegarRuta = { navController.navegarDesdeAviso(it) },
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(vertical = 8.dp),
                ) {
                    DetalleNotificacionPanel(
                        item = uiState.notificacionSeleccionada,
                        onMarcarLeida = {
                            uiState.notificacionSeleccionada?.id?.let { viewModel.marcarComoLeida(it) }
                        },
                        onNavegarRuta = { navController.navegarDesdeAviso(it) },
                    )
                }
            }
        } else {
            // Vista en una sola columna para pantalla compacta
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
            ) {
                BarraFiltros(uiState = uiState, onFiltroChanged = viewModel::cambiarFiltro)
                ListaNotificaciones(
                    uiState = uiState,
                    onSeleccionar = viewModel::seleccionarNotificacion,
                    onMarcarLeida = viewModel::marcarComoLeida,
                    onNavegarRuta = { navController.navegarDesdeAviso(it) },
                )
            }
        }

        // Diálogo animado de creación de aviso
        AnimatedVisibility(
            visible = uiState.mostrarDialogoCrear,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f),
        ) {
            DialogoCrearAviso(
                uiState = uiState,
                onDestinatarioChange = viewModel::onDestinatarioChanged,
                onTituloChange = viewModel::onNuevoTituloChanged,
                onMensajeChange = viewModel::onNuevoMensajeChanged,
                onRutaChange = viewModel::onNuevaRutaChanged,
                onDismiss = { viewModel.setMostrarDialogoCrear(mostrar = false) },
                onConfirmar = { viewModel.publicarAvisoDePrueba() },
            )
        }
    }
}

@Composable
private fun BarraFiltros(
    uiState: NotificacionesUiState,
    onFiltroChanged: (FiltroNotificacion) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = uiState.filtroActual == FiltroNotificacion.TODAS,
            onClick = { onFiltroChanged(FiltroNotificacion.TODAS) },
            label = { Text(stringResource(R.string.notif_filtro_todas)) },
        )
        FilterChip(
            selected = uiState.filtroActual == FiltroNotificacion.NO_LEIDAS,
            onClick = { onFiltroChanged(FiltroNotificacion.NO_LEIDAS) },
            label = { Text("${stringResource(R.string.notif_filtro_no_leidas)} (${uiState.noLeidasCount})") },
        )
    }
}

@Composable
private fun ListaNotificaciones(
    uiState: NotificacionesUiState,
    onSeleccionar: (NotificationItem) -> Unit,
    onMarcarLeida: (String) -> Unit,
    onNavegarRuta: (String) -> Unit,
) {
    if (uiState.lista.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.NotificationsNone,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.notif_sin_notificaciones),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(uiState.lista, key = { it.id }) { item ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { 20 }),
                ) {
                    TarjetaNotificacion(
                        item = item,
                        esSeleccionada = item.id == uiState.notificacionSeleccionada?.id,
                        onClick = { onSeleccionar(item) },
                        onMarcarLeida = { onMarcarLeida(item.id) },
                        onNavegarRuta = onNavegarRuta,
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaNotificacion(
    item: NotificationItem,
    esSeleccionada: Boolean,
    onClick: () -> Unit,
    onMarcarLeida: () -> Unit,
    onNavegarRuta: (String) -> Unit,
) {
    val fechaFormateada = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(item.fechaMillis))

    // Tarea 2: Transición de color animada al marcar como leída
    val colorFondoAnimado by animateColorAsState(
        targetValue = when {
            esSeleccionada -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            !item.leida -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else -> MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = tween(500),
        label = "colorFondoNotificacion",
    )

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondoAnimado),
        elevation = CardDefaults.cardElevation(defaultElevation = if (!item.leida) 3.dp else 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    if (!item.leida) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = item.titulo,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Text(
                    text = fechaFormateada,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!item.leida) {
                    TextButton(onClick = onMarcarLeida) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.notif_marcar_leida))
                    }
                }

                if (item.ruta != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = { onNavegarRuta(item.ruta) }) {
                        Text(stringResource(R.string.notif_ir_a_seccion))
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetalleNotificacionPanel(
    item: NotificationItem?,
    onMarcarLeida: () -> Unit,
    onNavegarRuta: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        if (item == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.notif_seleccionar_indicacion),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            val fechaFormateada = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(item.fechaMillis))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
            ) {
                Text(
                    text = stringResource(R.string.notif_detalle_titulo),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = item.titulo,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = fechaFormateada,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = item.mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    if (!item.leida) {
                        TextButton(onClick = onMarcarLeida) {
                            Icon(imageVector = Icons.Filled.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.notif_marcar_leida))
                        }
                    }

                    if (item.ruta != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = { onNavegarRuta(item.ruta) }) {
                            Text(stringResource(R.string.notif_ir_a_seccion))
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoCrearAviso(
    uiState: NotificacionesUiState,
    onDestinatarioChange: (OpcionDestinatario) -> Unit,
    onTituloChange: (String) -> Unit,
    onMensajeChange: (String) -> Unit,
    onRutaChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirmar: () -> Unit,
) {
    var expandedDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.notif_dialog_titulo),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Selector de Destinatario (Tarea 1)
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown },
                ) {
                    OutlinedTextField(
                        value = stringResource(uiState.destinatarioSeleccionado.labelRes),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.notif_campo_destinatario)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )

                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false },
                    ) {
                        OpcionDestinatario.entries.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(stringResource(opcion.labelRes)) },
                                onClick = {
                                    onDestinatarioChange(opcion)
                                    expandedDropdown = false
                                },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.nuevoTitulo,
                    onValueChange = onTituloChange,
                    label = { Text(stringResource(R.string.notif_campo_titulo)) },
                    isError = uiState.errorTituloRes != null,
                    supportingText = {
                        uiState.errorTituloRes?.let { resId ->
                            Text(text = stringResource(resId), color = MaterialTheme.colorScheme.error)
                        }
                    },
                    trailingIcon = {
                        if (uiState.errorTituloRes != null) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = uiState.nuevoMensaje,
                    onValueChange = onMensajeChange,
                    label = { Text(stringResource(R.string.notif_campo_mensaje)) },
                    isError = uiState.errorMensajeRes != null,
                    supportingText = {
                        uiState.errorMensajeRes?.let { resId ->
                            Text(text = stringResource(resId), color = MaterialTheme.colorScheme.error)
                        }
                    },
                    trailingIcon = {
                        if (uiState.errorMensajeRes != null) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = uiState.nuevaRuta,
                    onValueChange = onRutaChange,
                    label = { Text(stringResource(R.string.notif_campo_ruta)) },
                    singleLine = true,
                    placeholder = { Text("ej. catalogo, mapa, historial") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text(stringResource(R.string.notif_btn_publicar))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.notif_btn_cancelar))
            }
        },
    )
}
