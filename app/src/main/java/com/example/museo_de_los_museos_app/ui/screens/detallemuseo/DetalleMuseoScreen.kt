package com.example.museo_de_los_museos_app.ui.screens.detallemuseo

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.model.ClasificacionMuseo
import com.example.museo_de_los_museos_app.model.Museum
import com.example.museo_de_los_museos_app.ui.components.BannerMensaje
import com.example.museo_de_los_museos_app.ui.components.BotonPrincipal
import com.example.museo_de_los_museos_app.ui.components.CampoTextoValidado
import com.example.museo_de_los_museos_app.ui.utils.texto
import com.example.museo_de_los_museos_app.viewmodel.detallemuseo.DetalleMuseoUiState
import com.example.museo_de_los_museos_app.viewmodel.detallemuseo.DetalleMuseoViewModel
import com.example.museo_de_los_museos_app.viewmodel.detallemuseo.MensajeDetalle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val ROJO_FAVORITO = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleMuseoScreen(
    navController: NavHostController,
    museoId: String,
    viewModel: DetalleMuseoViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(museoId) { viewModel.cargar(museoId) }

    val textoMensaje = estado.mensaje?.let {
        stringResource(
            when (it) {
                MensajeDetalle.FAVORITO_AGREGADO -> R.string.detalle_favorito_agregado
                MensajeDetalle.FAVORITO_QUITADO -> R.string.detalle_favorito_quitado
                MensajeDetalle.SIN_SESION -> R.string.detalle_favorito_sin_sesion
            }
        )
    }
    LaunchedEffect(textoMensaje) {
        if (textoMensaje != null) {
            // Se lanza aparte para que el Snackbar no se corte cuando el mensaje vuelve a null
            alcance.launch { snackbar.showSnackbar(textoMensaje) }
            viewModel.onMensajeMostrado()
        }
    }

    // Ficha nueva guardada: muestra la confirmación un momento y vuelve al mapa
    LaunchedEffect(estado.cerrar) {
        if (estado.cerrar) {
            if (estado.fichaGuardada) delay(1200)
            navController.popBackStack()
        }
    }

    val textoErrorEnlace = stringResource(R.string.detalle_recorrido_error)
    val abrirEnlace: (String) -> Unit = { enlace ->
        // Si el teléfono no tiene navegador, se avisa en vez de cerrar la app
        runCatching { uriHandler.openUri(enlace) }
            .onFailure { alcance.launch { snackbar.showSnackbar(textoErrorEnlace) } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            estado.esNuevo -> stringResource(R.string.detalle_titulo_nueva)
                            estado.editando -> stringResource(R.string.detalle_titulo_editar)
                            else -> stringResource(R.string.pantalla_detalle_museo)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.p3_volver))
                    }
                },
                actions = {
                    if (estado.esInvestigador && estado.museo != null && !estado.editando) {
                        IconButton(onClick = viewModel::onEditar) {
                            Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.detalle_editar_ficha))
                        }
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                estado.cargando -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                estado.noEncontrado -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text(stringResource(R.string.detalle_no_encontrado))
                    Button(onClick = { navController.popBackStack() }) { Text(stringResource(R.string.p3_volver)) }
                }

                estado.esNuevo && !estado.esInvestigador -> Text(
                    text = stringResource(R.string.detalle_solo_investigador),
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )

                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnimatedVisibility(visible = estado.fichaGuardada) {
                        BannerMensaje(texto = stringResource(R.string.ficha_guardada), esError = false)
                    }

                    // Cambia entre la ficha y el formulario con un fundido
                    AnimatedContent(
                        targetState = estado.editando,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "fichaOFormulario"
                    ) { editando ->
                        if (editando) {
                            FormularioFichaMuseo(estado = estado, viewModel = viewModel)
                        } else {
                            estado.museo?.let { museo ->
                                VistaMuseo(
                                    museo = museo,
                                    esFavorito = estado.esFavorito,
                                    alAlternarFavorito = viewModel::alternarFavorito,
                                    alAbrirRecorrido = { abrirEnlace(museo.enlace) }
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
private fun VistaMuseo(
    museo: Museum,
    esFavorito: Boolean,
    alAlternarFavorito: () -> Unit,
    alAbrirRecorrido: () -> Unit
) {
    // Corazón que "late" y cambia de color al marcar favorito
    val escala by animateFloatAsState(
        targetValue = if (esFavorito) 1.25f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium),
        label = "escalaFavorito"
    )
    val colorCorazon by animateColorAsState(
        targetValue = if (esFavorito) ROJO_FAVORITO else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "colorFavorito"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Museum,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = museo.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = alAlternarFavorito) {
                        Icon(
                            imageVector = if (esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = stringResource(
                                if (esFavorito) R.string.detalle_quitar_favorito else R.string.detalle_agregar_favorito
                            ),
                            tint = colorCorazon,
                            modifier = Modifier.scale(escala)
                        )
                    }
                }
                AssistChip(onClick = {}, label = { Text(museo.clasificacion.etiqueta) })
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.detalle_ubicacion, museo.ciudad, museo.pais))
                }
                Text(
                    text = stringResource(R.string.detalle_coordenadas, museo.latitud, museo.longitud),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.detalle_proyecto, museo.proyecto),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(text = museo.descripcion, style = MaterialTheme.typography.bodyLarge)

        Button(onClick = alAbrirRecorrido, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text(stringResource(R.string.detalle_recorrido))
        }
        OutlinedButton(onClick = alAlternarFavorito, modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = if (esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = null,
                tint = colorCorazon
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(stringResource(if (esFavorito) R.string.detalle_quitar_favorito else R.string.detalle_agregar_favorito))
        }
    }
}

@Composable
private fun FormularioFichaMuseo(estado: DetalleMuseoUiState, viewModel: DetalleMuseoViewModel) {
    val formulario = estado.formulario
    val errores = estado.errores
    val mostrarValido = estado.intentoEnvio

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        CampoTextoValidado(
            valor = formulario.nombre,
            alCambiar = viewModel::onNombreChange,
            etiqueta = stringResource(R.string.ficha_nombre),
            iconoInicial = Icons.Filled.Museum,
            mensajeError = errores.nombre.texto(),
            mostrarValido = mostrarValido && errores.nombre == null
        )
        CampoTextoValidado(
            valor = formulario.pais,
            alCambiar = viewModel::onPaisChange,
            etiqueta = stringResource(R.string.ficha_pais),
            iconoInicial = Icons.Filled.Flag,
            mensajeError = errores.pais.texto(),
            mostrarValido = mostrarValido && errores.pais == null
        )
        CampoTextoValidado(
            valor = formulario.ciudad,
            alCambiar = viewModel::onCiudadChange,
            etiqueta = stringResource(R.string.ficha_ciudad),
            iconoInicial = Icons.Filled.LocationCity,
            mensajeError = errores.ciudad.texto(),
            mostrarValido = mostrarValido && errores.ciudad == null
        )
        // Teclado de texto (y no numérico) para poder escribir el signo menos en todos los teléfonos
        CampoTextoValidado(
            valor = formulario.latitud,
            alCambiar = viewModel::onLatitudChange,
            etiqueta = stringResource(R.string.ficha_latitud),
            iconoInicial = Icons.Filled.NorthEast,
            mensajeError = errores.latitud.texto(),
            tipoTeclado = KeyboardType.Text,
            mostrarValido = mostrarValido && errores.latitud == null
        )
        CampoTextoValidado(
            valor = formulario.longitud,
            alCambiar = viewModel::onLongitudChange,
            etiqueta = stringResource(R.string.ficha_longitud),
            iconoInicial = Icons.Filled.SouthEast,
            mensajeError = errores.longitud.texto(),
            tipoTeclado = KeyboardType.Text,
            mostrarValido = mostrarValido && errores.longitud == null
        )

        Text(
            text = stringResource(R.string.ficha_clasificacion),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 4.dp)
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ClasificacionMuseo.entries.forEach { clasificacion ->
                FilterChip(
                    selected = formulario.clasificacion == clasificacion,
                    onClick = { viewModel.onClasificacionChange(clasificacion) },
                    label = { Text(clasificacion.etiqueta) }
                )
            }
        }

        // Descripción en varias líneas: mismo estilo de error que CampoTextoValidado
        val errorDescripcion = errores.descripcion.texto()
        OutlinedTextField(
            value = formulario.descripcion,
            onValueChange = viewModel::onDescripcionChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.ficha_descripcion)) },
            leadingIcon = { Icon(Icons.Filled.Description, contentDescription = null) },
            trailingIcon = {
                if (errorDescripcion != null) {
                    Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                }
            },
            isError = errorDescripcion != null,
            supportingText = { if (errorDescripcion != null) Text(errorDescripcion) },
            minLines = 3
        )
        CampoTextoValidado(
            valor = formulario.enlace,
            alCambiar = viewModel::onEnlaceChange,
            etiqueta = stringResource(R.string.ficha_enlace),
            iconoInicial = Icons.Filled.Link,
            mensajeError = errores.enlace.texto(),
            tipoTeclado = KeyboardType.Uri,
            mostrarValido = mostrarValido && errores.enlace == null
        )

        Spacer(modifier = Modifier.height(8.dp))
        BotonPrincipal(
            texto = stringResource(R.string.ficha_guardar),
            alHacerClick = viewModel::guardar,
            cargando = estado.guardando,
            habilitado = !estado.cerrar
        )
        TextButton(onClick = viewModel::onCancelarEdicion, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(stringResource(R.string.ficha_cancelar))
        }
    }
}
