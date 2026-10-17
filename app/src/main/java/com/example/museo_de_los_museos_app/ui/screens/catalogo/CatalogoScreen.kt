package com.example.museo_de_los_museos_app.ui.screens.catalogo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.model.CategoriaProducto
import com.example.museo_de_los_museos_app.model.Product
import com.example.museo_de_los_museos_app.navigation.Routes
import com.example.museo_de_los_museos_app.ui.utils.formatearPrecio

/** Ancho desde el cual se usa la vista expandida (cuadrícula), como en la Guía 9. */
private val ANCHO_EXPANDIDO = 600.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    navController: NavHostController,
    viewModel: CatalogoViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pantalla_catalogo)) },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.Historial.route) }) {
                        Icon(Icons.Filled.History, contentDescription = stringResource(R.string.catalogo_ir_historial))
                    }
                    IconButton(onClick = { navController.navigate(Routes.Carrito.route) }) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = stringResource(R.string.catalogo_ir_carrito))
                    }
                },
                // Las barras del sistema ya las maneja el Scaffold del NavHost
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            BarraBusqueda(
                texto = estado.busqueda,
                onTextoChange = viewModel::onBusquedaChange
            )
            FiltrosCatalogo(
                categoriaSeleccionada = estado.categoriaSeleccionada,
                soloDisponibles = estado.soloDisponibles,
                onCategoriaSeleccionada = viewModel::onCategoriaSeleccionada,
                onSoloDisponiblesChange = viewModel::onSoloDisponiblesChange
            )
            Text(
                text = stringResource(R.string.catalogo_resultados, estado.productos.size),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            AnimatedVisibility(
                visible = estado.productos.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                SinResultados(
                    mostrarLimpiar = estado.hayFiltrosActivos,
                    onLimpiar = viewModel::limpiarFiltros
                )
            }

            ListaProductos(
                productos = estado.productos,
                esAdministrador = estado.esAdministrador,
                onProductoClick = { producto ->
                    navController.navigate(Routes.DetalleProducto.crear(producto.id))
                }
            )
        }
    }
}

@Composable
private fun BarraBusqueda(texto: String, onTextoChange: (String) -> Unit) {
    OutlinedTextField(
        value = texto,
        onValueChange = onTextoChange,
        label = { Text(stringResource(R.string.catalogo_buscar)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (texto.isNotEmpty()) {
                IconButton(onClick = { onTextoChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.catalogo_limpiar_busqueda))
                }
            }
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun FiltrosCatalogo(
    categoriaSeleccionada: CategoriaProducto?,
    soloDisponibles: Boolean,
    onCategoriaSeleccionada: (CategoriaProducto?) -> Unit,
    onSoloDisponiblesChange: (Boolean) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        FilterChip(
            selected = categoriaSeleccionada == null,
            onClick = { onCategoriaSeleccionada(null) },
            label = { Text(stringResource(R.string.catalogo_todas)) }
        )
        CategoriaProducto.entries.forEach { categoria ->
            val seleccionada = categoria == categoriaSeleccionada
            FilterChip(
                selected = seleccionada,
                onClick = { onCategoriaSeleccionada(categoria) },
                label = { Text(categoria.etiqueta) },
                leadingIcon = if (seleccionada) {
                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )
        }
        FilterChip(
            selected = soloDisponibles,
            onClick = { onSoloDisponiblesChange(!soloDisponibles) },
            label = { Text(stringResource(R.string.catalogo_solo_disponibles)) }
        )
    }
}

/** Vista compacta: una columna. Vista expandida (tablet u horizontal): cuadrícula. */
@Composable
private fun ListaProductos(
    productos: List<Product>,
    esAdministrador: Boolean,
    onProductoClick: (Product) -> Unit
) {
    val espaciado = PaddingValues(16.dp)
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (maxWidth < ANCHO_EXPANDIDO) {
            LazyColumn(
                contentPadding = espaciado,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(productos, key = { it.id }) { producto ->
                    TarjetaProducto(
                        producto = producto,
                        esAdministrador = esAdministrador,
                        onClick = { onProductoClick(producto) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 280.dp),
                contentPadding = espaciado,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(productos, key = { it.id }) { producto ->
                    TarjetaProducto(
                        producto = producto,
                        esAdministrador = esAdministrador,
                        onClick = { onProductoClick(producto) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaProducto(
    producto: Product,
    esAdministrador: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Color animado según disponibilidad (retroalimentación visual, Guía 12)
    val colorDisponibilidad by animateColorAsState(
        targetValue = if (producto.disponible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        label = "colorDisponibilidad"
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (producto.disponible) 1f else 0.7f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = iconoCategoria(producto.categoria),
                contentDescription = producto.categoria.etiqueta,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = producto.categoria.etiqueta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatearPrecio(producto.precio),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = textoDisponibilidad(producto),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorDisponibilidad
                )
                if (esAdministrador && !producto.activo) {
                    AssistChip(
                        onClick = onClick,
                        label = { Text(stringResource(R.string.catalogo_desactivado)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun textoDisponibilidad(producto: Product): String = when {
    !producto.activo -> stringResource(R.string.catalogo_no_disponible)
    producto.stock <= 0 -> stringResource(R.string.catalogo_agotado)
    producto.categoria == CategoriaProducto.CURSO -> stringResource(R.string.catalogo_cupos, producto.stock)
    else -> stringResource(R.string.catalogo_stock, producto.stock)
}

private fun iconoCategoria(categoria: CategoriaProducto): ImageVector = when (categoria) {
    CategoriaProducto.CURSO -> Icons.Filled.School
    CategoriaProducto.PUBLICACION -> Icons.AutoMirrored.Filled.MenuBook
    CategoriaProducto.MATERIAL_CONSERVACION -> Icons.Filled.Inventory2
    CategoriaProducto.ARTICULO_MUSEOLOGICO -> Icons.Filled.ShoppingBag
}

@Composable
private fun SinResultados(mostrarLimpiar: Boolean, onLimpiar: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        Icon(
            Icons.Filled.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.catalogo_sin_resultados),
            style = MaterialTheme.typography.bodyLarge
        )
        if (mostrarLimpiar) {
            TextButton(onClick = onLimpiar) { Text(stringResource(R.string.catalogo_limpiar_filtros)) }
        }
    }
}
