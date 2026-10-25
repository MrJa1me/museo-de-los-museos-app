package com.example.museo_de_los_museos_app.ui.screens.formularioproducto

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.model.CategoriaProducto
import com.example.museo_de_los_museos_app.ui.components.BotonPrincipal
import com.example.museo_de_los_museos_app.ui.components.CampoTextoValidado
import com.example.museo_de_los_museos_app.ui.utils.texto
import com.example.museo_de_los_museos_app.viewmodel.formularioproducto.FormularioProductoUiState
import com.example.museo_de_los_museos_app.viewmodel.formularioproducto.FormularioProductoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioProductoScreen(
    navController: NavHostController,
    productoId: String?,
    viewModel: FormularioProductoViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(productoId) { viewModel.cargar(productoId) }

    // Al guardar, vuelve a la pantalla anterior (Catálogo o Detalle), que se actualiza sola
    LaunchedEffect(estado.guardado) {
        if (estado.guardado) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (estado.esEdicion) R.string.formulario_titulo_editar else R.string.formulario_titulo_nuevo))
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

                !estado.esAdministrador -> MensajeCentral(
                    icono = Icons.Filled.Lock,
                    texto = stringResource(R.string.formulario_solo_admin),
                    alVolver = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.Center)
                )

                estado.noEncontrado -> MensajeCentral(
                    icono = Icons.Filled.SearchOff,
                    texto = stringResource(R.string.detalle_producto_no_encontrado),
                    alVolver = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> Formulario(estado = estado, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun Formulario(estado: FormularioProductoUiState, viewModel: FormularioProductoViewModel) {
    val e = estado.errores
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Tipo de ítem: curso o producto físico (y su categoría)
        Text(stringResource(R.string.formulario_tipo), style = MaterialTheme.typography.titleSmall)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
        ) {
            CategoriaProducto.entries.forEach { categoria ->
                val seleccionada = estado.categoria == categoria
                FilterChip(
                    selected = seleccionada,
                    onClick = { viewModel.onCategoriaChange(categoria) },
                    label = { Text(categoria.etiqueta) },
                    leadingIcon = if (seleccionada) {
                        { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }
        }
        Text(
            stringResource(if (estado.esCurso) R.string.formulario_ayuda_curso else R.string.formulario_ayuda_producto),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CampoTextoValidado(
            valor = estado.nombre,
            alCambiar = viewModel::onNombreChange,
            etiqueta = stringResource(R.string.formulario_nombre),
            iconoInicial = Icons.AutoMirrored.Filled.Label,
            mensajeError = e.nombre.texto()
        )

        // La descripción es opcional y puede tener varias líneas
        OutlinedTextField(
            value = estado.descripcion,
            onValueChange = viewModel::onDescripcionChange,
            label = { Text(stringResource(R.string.formulario_descripcion)) },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        CampoTextoValidado(
            valor = estado.precio,
            alCambiar = viewModel::onPrecioChange,
            etiqueta = stringResource(R.string.formulario_precio),
            iconoInicial = Icons.Filled.AttachMoney,
            mensajeError = e.precio.texto(),
            tipoTeclado = KeyboardType.Number
        )

        CampoTextoValidado(
            valor = estado.stock,
            alCambiar = viewModel::onStockChange,
            etiqueta = stringResource(if (estado.esCurso) R.string.formulario_cupos else R.string.formulario_stock),
            iconoInicial = Icons.Filled.Inventory,
            mensajeError = e.stock.texto(),
            tipoTeclado = KeyboardType.Number
        )

        // El peso solo aparece para productos físicos (lo usa la calculadora de envío)
        AnimatedVisibility(
            visible = !estado.esCurso,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            CampoTextoValidado(
                valor = estado.peso,
                alCambiar = viewModel::onPesoChange,
                etiqueta = stringResource(R.string.formulario_peso),
                iconoInicial = Icons.Filled.Scale,
                mensajeError = e.peso.texto(),
                tipoTeclado = KeyboardType.Decimal
            )
        }

        BotonPrincipal(
            texto = stringResource(if (estado.esEdicion) R.string.formulario_guardar_cambios else R.string.formulario_crear),
            alHacerClick = viewModel::guardar,
            cargando = estado.guardando,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun MensajeCentral(icono: ImageVector, texto: String, alVolver: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icono, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Text(texto, textAlign = TextAlign.Center)
        TextButton(onClick = alVolver) { Text(stringResource(R.string.p4_volver)) }
    }
}
