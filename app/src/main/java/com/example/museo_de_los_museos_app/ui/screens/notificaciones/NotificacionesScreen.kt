package com.example.museo_de_los_museos_app.ui.screens.notificaciones

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.model.NotificationItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla de Notificaciones y Avisos de la app (P2).
 * Permite filtrar por leídas/no leídas, marcar como leídas, navegar a pantallas relacionadas
 * y crear avisos de prueba si el usuario tiene rol Administrador.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    navController: NavHostController,
    viewModel: NotificacionesViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
            if (uiState.esAdmin) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {
            // Chips de filtrado: Todas vs No leídas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = uiState.filtroActual == FiltroNotificacion.TODAS,
                    onClick = { viewModel.cambiarFiltro(FiltroNotificacion.TODAS) },
                    label = { Text(stringResource(R.string.notif_filtro_todas)) },
                )
                FilterChip(
                    selected = uiState.filtroActual == FiltroNotificacion.NO_LEIDAS,
                    onClick = { viewModel.cambiarFiltro(FiltroNotificacion.NO_LEIDAS) },
                    label = { Text("${stringResource(R.string.notif_filtro_no_leidas)} (${uiState.noLeidasCount})") },
                )
            }

            if (uiState.lista.isEmpty()) {
                // Estado vacío
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
                // Lista de notificaciones
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(uiState.lista, key = { it.id }) { item ->
                        TarjetaNotificacion(
                            item = item,
                            onMarcarLeida = { viewModel.marcarComoLeida(item.id) },
                            onNavegarRuta = { navController.navigate(it) },
                        )
                    }
                }
            }
        }

        // Diálogo para que el Administrador cree un aviso de prueba
        if (uiState.mostrarDialogoCrear) {
            DialogoCrearAviso(
                uiState = uiState,
                onTituloChange = { viewModel.onNuevoTituloChanged(it) },
                onMensajeChange = { viewModel.onNuevoMensajeChanged(it) },
                onRutaChange = { viewModel.onNuevaRutaChanged(it) },
                onDismiss = { viewModel.setMostrarDialogoCrear(false) },
                onConfirmar = { viewModel.publicarAvisoDePrueba() },
            )
        }
    }
}

@Composable
private fun TarjetaNotificacion(
    item: NotificationItem,
    onMarcarLeida: () -> Unit,
    onNavegarRuta: (String) -> Unit,
) {
    val fechaFormateada = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(item.fechaMillis))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!item.leida) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
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
private fun DialogoCrearAviso(
    uiState: NotificacionesUiState,
    onTituloChange: (String) -> Unit,
    onMensajeChange: (String) -> Unit,
    onRutaChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirmar: () -> Unit,
) {
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
                OutlinedTextField(
                    value = uiState.nuevoTitulo,
                    onValueChange = onTituloChange,
                    label = { Text(stringResource(R.string.notif_campo_titulo)) },
                    isError = uiState.errorTitulo != null,
                    supportingText = {
                        uiState.errorTitulo?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    trailingIcon = {
                        if (uiState.errorTitulo != null) {
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
                    isError = uiState.errorMensaje != null,
                    supportingText = {
                        uiState.errorMensaje?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    trailingIcon = {
                        if (uiState.errorMensaje != null) {
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
            TextButton(onClick = { onConfirmar() }) {
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
