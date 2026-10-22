package com.example.museo_de_los_museos_app.ui.screens.mapa

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.model.Museum
import com.example.museo_de_los_museos_app.navigation.Routes
import com.example.museo_de_los_museos_app.viewmodel.detallemuseo.DetalleMuseoViewModel
import com.example.museo_de_los_museos_app.viewmodel.mapa.EstadoPermiso
import com.example.museo_de_los_museos_app.viewmodel.mapa.MapaViewModel
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

/** Punto inicial: centro de Iberoamérica con zoom 3 (plan, sección 9.3). */
private val CENTRO_IBEROAMERICA = GeoPoint(10.0, -40.0)
private const val ZOOM_INICIAL = 3.0
private const val ZOOM_PAIS = 5.0
private const val ZOOM_UBICACION = 12.0
private const val VELOCIDAD_ANIMACION_MS = 800L

private val PERMISOS_UBICACION = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen(
    navController: NavHostController,
    viewModel: MapaViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()
    val textoBuscando = stringResource(R.string.mapa_buscando_ubicacion)

    // ---------- Recurso nativo: ubicación (permiso en tiempo de ejecución) ----------
    val lanzadorPermisos = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultados -> viewModel.onResultadoPermiso(resultados.values.any { it }) }

    LaunchedEffect(Unit) {
        viewModel.onPermisoVerificado(
            PERMISOS_UBICACION.any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
        )
    }
    LaunchedEffect(estado.solicitarPermiso) {
        if (estado.solicitarPermiso) {
            viewModel.onSolicitudLanzada()
            lanzadorPermisos.launch(PERMISOS_UBICACION)
        }
    }

    // ---------- Mapa osmdroid conectado al ciclo de vida ----------
    val mapView = remember { crearMapa(context) }
    var capaUbicacion by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            capaUbicacion?.disableMyLocation()
            mapView.onPause()
            mapView.onDetach()
        }
    }

    // La capa "mi ubicación" solo se crea cuando hay permiso, así nunca se cae sin él.
    LaunchedEffect(estado.permisoUbicacion) {
        if (estado.permisoUbicacion == EstadoPermiso.CONCEDIDO && capaUbicacion == null) {
            val capa = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
            capa.enableMyLocation()
            mapView.overlays.add(capa)
            mapView.invalidate()
            capaUbicacion = capa
        }
    }

    LaunchedEffect(estado.centrarEnUbicacion, capaUbicacion) {
        val capa = capaUbicacion
        if (estado.centrarEnUbicacion && capa != null) {
            viewModel.onCentradoRealizado()
            val punto = capa.myLocation
            if (punto != null) {
                mapView.controller.animateTo(punto, ZOOM_UBICACION, VELOCIDAD_ANIMACION_MS)
            } else {
                capa.runOnFirstFix {
                    mapView.post {
                        capa.myLocation?.let { mapView.controller.animateTo(it, ZOOM_UBICACION, VELOCIDAD_ANIMACION_MS) }
                    }
                }
                alcance.launch { snackbar.showSnackbar(textoBuscando) }
            }
        }
    }

    // Al filtrar por país, el mapa viaja con animación hasta sus museos.
    LaunchedEffect(estado.paisSeleccionado) {
        val primero = estado.museos.firstOrNull()
        if (estado.paisSeleccionado != null && primero != null) {
            mapView.controller.animateTo(GeoPoint(primero.latitud, primero.longitud), ZOOM_PAIS, VELOCIDAD_ANIMACION_MS)
        } else if (estado.paisSeleccionado == null) {
            mapView.controller.animateTo(CENTRO_IBEROAMERICA, ZOOM_INICIAL, VELOCIDAD_ANIMACION_MS)
        }
    }

    if (estado.mostrarExplicacionPermiso) {
        AlertDialog(
            onDismissRequest = viewModel::onExplicacionRechazada,
            icon = { Icon(Icons.Filled.MyLocation, contentDescription = null) },
            title = { Text(stringResource(R.string.mapa_permiso_titulo)) },
            text = { Text(stringResource(R.string.mapa_permiso_explicacion)) },
            confirmButton = {
                TextButton(onClick = viewModel::onExplicacionAceptada) { Text(stringResource(R.string.mapa_permiso_permitir)) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onExplicacionRechazada) { Text(stringResource(R.string.mapa_permiso_ahora_no)) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pantalla_mapa)) },
                actions = {
                    if (estado.esInvestigador) {
                        IconButton(onClick = {
                            navController.navigate(Routes.DetalleMuseo.crear(DetalleMuseoViewModel.ID_NUEVO))
                        }) {
                            Icon(Icons.Filled.AddLocationAlt, contentDescription = stringResource(R.string.mapa_nueva_ficha))
                        }
                    }
                    IconButton(onClick = { navController.navigate(Routes.Favoritos.route) }) {
                        Icon(Icons.Filled.Favorite, contentDescription = stringResource(R.string.mapa_ir_favoritos))
                    }
                    IconButton(onClick = { navController.navigate(Routes.EscanerQr.route) }) {
                        Icon(Icons.Filled.QrCodeScanner, contentDescription = stringResource(R.string.mapa_ir_escaner))
                    }
                },
                // Las barras del sistema ya las maneja el Scaffold del NavHost
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            FiltroPaises(
                paises = estado.paises,
                seleccionado = estado.paisSeleccionado,
                alSeleccionar = viewModel::onPaisSeleccionado
            )
            Text(
                text = stringResource(R.string.mapa_contador, estado.museos.size) + " · " +
                    stringResource(R.string.mapa_sin_internet),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            AnimatedVisibility(visible = estado.permisoUbicacion == EstadoPermiso.DENEGADO) {
                AvisoPermisoDenegado(
                    alReintentar = viewModel::onExplicacionAceptada,
                    alAbrirAjustes = { abrirAjustesDeLaApp(context) }
                )
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 8.dp)) {
                AndroidView(
                    factory = { mapView },
                    update = { mapa -> dibujarMarcadores(mapa, estado.museos, viewModel::onMuseoSeleccionado) },
                    modifier = Modifier.fillMaxSize()
                )

                SmallFloatingActionButton(
                    onClick = viewModel::onMiUbicacionClick,
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                ) {
                    Icon(Icons.Filled.MyLocation, contentDescription = stringResource(R.string.mapa_mi_ubicacion))
                }

                // Recuerda el último museo para que la tarjeta pueda animar su salida.
                var ultimoMuseo by remember { mutableStateOf<Museum?>(null) }
                LaunchedEffect(estado.museoSeleccionado) { estado.museoSeleccionado?.let { ultimoMuseo = it } }

                androidx.compose.animation.AnimatedVisibility(
                    visible = estado.museoSeleccionado != null,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    (estado.museoSeleccionado ?: ultimoMuseo)?.let { museo ->
                        TarjetaMuseoSeleccionado(
                            museo = museo,
                            alVerFicha = { navController.navigate(Routes.DetalleMuseo.crear(museo.id)) },
                            alCerrar = viewModel::onCerrarSeleccion
                        )
                    }
                }
            }
        }
    }
}

private fun crearMapa(context: Context): MapView = MapView(context).apply {
    setTileSource(TileSourceFactory.MAPNIK)
    setMultiTouchControls(true)
    isTilesScaledToDpi = true
    setMinZoomLevel(2.0)
    controller.setZoom(ZOOM_INICIAL)
    controller.setCenter(CENTRO_IBEROAMERICA)
}

/** Un marcador por museo. Se rehacen cuando cambia el filtro (son pocos, no afecta el rendimiento). */
private fun dibujarMarcadores(mapa: MapView, museos: List<Museum>, alTocar: (String) -> Unit) {
    mapa.overlays.removeAll { it is Marker }
    museos.forEach { museo ->
        val marcador = Marker(mapa)
        marcador.position = GeoPoint(museo.latitud, museo.longitud)
        marcador.title = museo.nombre
        marcador.snippet = "${museo.ciudad}, ${museo.pais}"
        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marcador.setOnMarkerClickListener { _, _ ->
            alTocar(museo.id)
            true
        }
        mapa.overlays.add(marcador)
    }
    mapa.invalidate()
}

private fun abrirAjustesDeLaApp(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}

@Composable
private fun FiltroPaises(paises: List<String>, seleccionado: String?, alSeleccionar: (String?) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = seleccionado == null,
            onClick = { alSeleccionar(null) },
            label = { Text(stringResource(R.string.mapa_filtro_todos)) }
        )
        paises.forEach { pais ->
            FilterChip(
                selected = seleccionado == pais,
                onClick = { alSeleccionar(pais) },
                label = { Text(pais) }
            )
        }
    }
}

@Composable
private fun AvisoPermisoDenegado(alReintentar: () -> Unit, alAbrirAjustes: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOff, contentDescription = null)
                Text(stringResource(R.string.mapa_permiso_denegado), style = MaterialTheme.typography.bodyMedium)
            }
            Row(modifier = Modifier.align(Alignment.End)) {
                TextButton(onClick = alReintentar) { Text(stringResource(R.string.mapa_permiso_reintentar)) }
                TextButton(onClick = alAbrirAjustes) { Text(stringResource(R.string.mapa_permiso_ajustes)) }
            }
        }
    }
}

@Composable
private fun TarjetaMuseoSeleccionado(museo: Museum, alVerFicha: () -> Unit, alCerrar: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = museo.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = alCerrar) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.mapa_cerrar_seleccion))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = stringResource(R.string.detalle_ubicacion, museo.ciudad, museo.pais) + " · " + museo.clasificacion.etiqueta,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(onClick = alVerFicha, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(R.string.mapa_ver_ficha))
            }
        }
    }
}
