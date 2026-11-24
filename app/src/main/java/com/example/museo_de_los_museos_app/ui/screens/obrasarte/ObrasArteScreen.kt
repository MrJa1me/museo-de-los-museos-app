package com.example.museo_de_los_museos_app.ui.screens.obrasarte

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.SubcomposeAsyncImage
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.model.ObraArte
import com.example.museo_de_los_museos_app.remote.TipoErrorRemoto
import com.example.museo_de_los_museos_app.ui.utils.texto
import com.example.museo_de_los_museos_app.viewmodel.obrasarte.ObrasArteViewModel

/**
 * Obras de arte del mundo: consume la API externa del Art Institute of Chicago con Retrofit.
 * Es independiente de Room y de los microservicios propios (solo lectura, datos de terceros).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObrasArteScreen(
    navController: NavHostController,
    pais: String?,
    viewModel: ObrasArteViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val teclado = LocalSoftwareKeyboardController.current

    LaunchedEffect(pais) { viewModel.iniciar(pais) }

    val buscar = {
        teclado?.hide()
        viewModel.buscar()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.obras_titulo)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.p3_volver))
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                stringResource(R.string.obras_subtitulo),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            OutlinedTextField(
                value = estado.consulta,
                onValueChange = viewModel::onConsultaChange,
                label = { Text(stringResource(R.string.obras_campo_busqueda)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (estado.consultaInvalida) {
                        Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    }
                },
                isError = estado.consultaInvalida,
                supportingText = { if (estado.consultaInvalida) Text(stringResource(R.string.obras_error_corta)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { buscar() }),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp)
            ) {
                ObrasArteViewModel.SUGERENCIAS.forEach { sugerencia ->
                    FilterChip(
                        selected = estado.ultimaBusqueda == sugerencia,
                        onClick = { viewModel.onSugerencia(sugerencia) },
                        label = { Text(sugerencia) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                val error = estado.errorRemoto
                when {
                    estado.cargando -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                    error != null -> EstadoCentral(
                        icono = if (error.tipo == TipoErrorRemoto.SIN_CONEXION) Icons.Filled.CloudOff else Icons.Filled.Error,
                        texto = error.texto(),
                        textoBoton = stringResource(R.string.obras_reintentar),
                        alHacerClick = { buscar() },
                        modifier = Modifier.align(Alignment.Center)
                    )

                    estado.sinResultados -> EstadoCentral(
                        icono = Icons.Filled.Palette,
                        texto = stringResource(R.string.obras_vacio, estado.ultimaBusqueda.orEmpty()),
                        modifier = Modifier.align(Alignment.Center)
                    )

                    else -> LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(estado.obras, key = { it.id }) { obra ->
                            TarjetaObra(obra = obra, modifier = Modifier.animateItem())
                        }
                        item {
                            Text(
                                stringResource(R.string.obras_fuente),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaObra(obra: ObraArte, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val forma = RoundedCornerShape(12.dp)
            val miniatura = Modifier.size(96.dp).clip(forma)
            if (obra.urlImagen != null) {
                // Coil descarga la imagen desde el servidor IIIF del museo (en segundo plano y con caché)
                SubcomposeAsyncImage(
                    model = obra.urlImagen,
                    contentDescription = obra.titulo,
                    contentScale = ContentScale.Crop,
                    modifier = miniatura,
                    loading = {
                        Box(Modifier.background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    },
                    error = { SinImagen() }
                )
            } else {
                Box(miniatura) { SinImagen() }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                Text(obra.titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (obra.artista.isNotBlank()) {
                    Text(obra.artista, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                val detalle = listOf(obra.fecha, obra.origen).filter { it.isNotBlank() }.joinToString(" · ")
                if (detalle.isNotBlank()) {
                    Text(detalle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (obra.tecnica.isNotBlank()) {
                    Text(obra.tecnica, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun SinImagen() {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.ImageNotSupported, contentDescription = stringResource(R.string.obras_sin_imagen))
    }
}

@Composable
private fun EstadoCentral(
    icono: ImageVector,
    texto: String,
    modifier: Modifier = Modifier,
    textoBoton: String? = null,
    alHacerClick: () -> Unit = {}
) {
    AnimatedVisibility(visible = true, modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
            Text(texto, textAlign = TextAlign.Center)
            if (textoBoton != null) Button(onClick = alHacerClick) { Text(textoBoton) }
        }
    }
}
