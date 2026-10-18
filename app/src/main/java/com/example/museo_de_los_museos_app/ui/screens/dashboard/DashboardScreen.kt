package com.example.museo_de_los_museos_app.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.model.Museum
import com.example.museo_de_los_museos_app.model.Product
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.navigation.Routes

/**
 * Pantalla de Dashboard / Inicio adaptativa (P2).
 * Presenta métricas y contenido personalizado según el rol activo (Cliente, Administrador, Investigador),
 * permitiendo navegación directa a museos, catálogo, QR, notificaciones y solicitudes.
 */
@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val esPantallaAncha = configuration.screenWidthDp >= 600

    Scaffold { paddingValues ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp),
        ) {
            // Encabezado de bienvenida con rol activo
            EncabezadoUsuario(
                uiState = uiState,
                onNotificacionesClick = { navController.navigate(Routes.Notificaciones.route) },
            )

            if (esPantallaAncha) {
                // Vista adaptativa para pantallas anchas (tablets / horizontal)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        SeccionAccionesRapidas(navController = navController)
                        Spacer(modifier = Modifier.height(16.dp))
                        ContenidoPorRol(uiState = uiState, navController = navController)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        SeccionMuseosDestacados(
                            museos = uiState.museosDestacados,
                            navController = navController,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        SeccionCursosDestacados(
                            cursos = uiState.cursosDestacados,
                            navController = navController,
                        )
                    }
                }
            } else {
                // Vista compacta estándar (smartphones)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    SeccionAccionesRapidas(navController = navController)

                    Spacer(modifier = Modifier.height(20.dp))
                    ContenidoPorRol(uiState = uiState, navController = navController)

                    Spacer(modifier = Modifier.height(20.dp))
                    SeccionMuseosDestacados(
                        museos = uiState.museosDestacados,
                        navController = navController,
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    SeccionCursosDestacados(
                        cursos = uiState.cursosDestacados,
                        navController = navController,
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    SeccionProductosDestacados(
                        productos = uiState.productosDestacados,
                        navController = navController,
                    )
                }
            }
        }
    }
}

@Composable
private fun EncabezadoUsuario(
    uiState: DashboardUiState,
    onNotificacionesClick: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val saludo = if (uiState.usuario != null) {
                    stringResource(R.string.dashboard_saludo_hola, uiState.usuario.nombre)
                } else {
                    stringResource(R.string.dashboard_saludo_invitado)
                }

                Text(
                    text = saludo,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(4.dp))

                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = stringResource(R.string.dashboard_rol_label, uiState.rol.etiqueta),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    shape = RoundedCornerShape(12.dp),
                )
            }

            IconButton(
                onClick = onNotificacionesClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = stringResource(R.string.pantalla_notificaciones),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun SeccionAccionesRapidas(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.dashboard_seccion_acciones),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BotonAccionRapida(
                icono = Icons.Filled.QrCodeScanner,
                etiqueta = stringResource(R.string.dashboard_btn_escaner),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Routes.EscanerQr.route) },
            )
            BotonAccionRapida(
                icono = Icons.Filled.Favorite,
                etiqueta = stringResource(R.string.dashboard_btn_favoritos),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Routes.Favoritos.route) },
            )
            BotonAccionRapida(
                icono = Icons.Filled.ShoppingCart,
                etiqueta = stringResource(R.string.dashboard_btn_carrito),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Routes.Carrito.route) },
            )
            BotonAccionRapida(
                icono = Icons.Filled.History,
                etiqueta = stringResource(R.string.dashboard_btn_historial),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Routes.Historial.route) },
            )
        }
    }
}

@Composable
private fun BotonAccionRapida(
    icono: ImageVector,
    etiqueta: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icono,
                contentDescription = etiqueta,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelSmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ContenidoPorRol(
    uiState: DashboardUiState,
    navController: NavHostController,
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(),
    ) {
        when (uiState.rol) {
            Rol.ADMINISTRADOR -> PanelAdmin(uiState = uiState, navController = navController)
            Rol.INVESTIGADOR -> PanelInvestigador(uiState = uiState, navController = navController)
            Rol.CLIENTE -> PanelClienteBanner(navController = navController)
        }
    }
}

@Composable
private fun PanelAdmin(
    uiState: DashboardUiState,
    navController: NavHostController,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.dashboard_admin_titulo),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.dashboard_admin_metricas),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TarjetaMetrica(
                    titulo = "Solicitudes",
                    valor = "${uiState.solicitudesPendientesCount} pendientes",
                    icono = Icons.Filled.ConfirmationNumber,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Routes.Historial.route) },
                )
                TarjetaMetrica(
                    titulo = "Catálogo",
                    valor = "${uiState.totalProductos} ítems",
                    icono = Icons.Filled.Category,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Routes.Catalogo.route) },
                )
            }
        }
    }
}

@Composable
private fun PanelInvestigador(
    uiState: DashboardUiState,
    navController: NavHostController,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.dashboard_investigador_titulo),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.dashboard_investigador_fichas),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TarjetaMetrica(
                    titulo = "Mapa Museos",
                    valor = "${uiState.totalMuseos} fichas",
                    icono = Icons.Filled.Map,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Routes.Mapa.route) },
                )
                TarjetaMetrica(
                    titulo = "Escáner QR",
                    valor = "Captura rápida",
                    icono = Icons.Filled.QrCodeScanner,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Routes.EscanerQr.route) },
                )
            }
        }
    }
}

@Composable
private fun PanelClienteBanner(navController: NavHostController) {
    ElevatedCard(
        onClick = { navController.navigate(Routes.Mapa.route) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountBalance,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mapa de Museos Virtuales",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Explora los espacios culturales de Iberoamérica de forma interactiva.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun TarjetaMetrica(
    titulo: String,
    valor: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = valor,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SeccionMuseosDestacados(
    museos: List<Museum>,
    navController: NavHostController,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.dashboard_seccion_museos_destacados),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
            TextButton(onClick = { navController.navigate(Routes.Mapa.route) }) {
                Text(stringResource(R.string.dashboard_btn_ver_todos))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 12.dp),
        ) {
            items(museos, key = { it.id }) { museo ->
                TarjetaMuseo(
                    museo = museo,
                    onClick = { navController.navigate(Routes.DetalleMuseo.crear(museo.id)) },
                )
            }
        }
    }
}

@Composable
private fun TarjetaMuseo(
    museo: Museum,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountBalance,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(36.dp),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = museo.nombre,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${museo.ciudad}, ${museo.pais}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            AssistChip(
                onClick = onClick,
                label = {
                    Text(
                        text = museo.clasificacion.etiqueta,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                modifier = Modifier.height(26.dp),
            )
        }
    }
}

@Composable
private fun SeccionCursosDestacados(
    cursos: List<Product>,
    navController: NavHostController,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.dashboard_seccion_cursos_destacados),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
            TextButton(onClick = { navController.navigate(Routes.Catalogo.route) }) {
                Text(stringResource(R.string.dashboard_btn_ver_todos))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            cursos.forEach { curso ->
                ItemCursoFila(
                    curso = curso,
                    onClick = { navController.navigate(Routes.DetalleProducto.crear(curso.id)) },
                )
            }
        }
    }
}

@Composable
private fun ItemCursoFila(
    curso: Product,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = curso.nombre,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${curso.stock} cupos disponibles",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = "$ ${String.format("%,d", curso.precio).replace(',', '.')}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun SeccionProductosDestacados(
    productos: List<Product>,
    navController: NavHostController,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.dashboard_seccion_productos_destacados),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp),
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(end = 12.dp),
        ) {
            items(productos, key = { it.id }) { producto ->
                TarjetaProductoCompacta(
                    producto = producto,
                    onClick = { navController.navigate(Routes.DetalleProducto.crear(producto.id)) },
                )
            }
        }
    }
}

@Composable
private fun TarjetaProductoCompacta(
    producto: Product,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Book,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = producto.categoria.etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$ ${String.format("%,d", producto.precio).replace(',', '.')}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
