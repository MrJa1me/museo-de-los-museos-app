package com.example.museo_de_los_museos_app.ui.screens.favoritos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.model.Museum
import com.example.museo_de_los_museos_app.navigation.Routes
import com.example.museo_de_los_museos_app.viewmodel.favoritos.FavoritosViewModel

private val ROJO_FAVORITO = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritosScreen(
    navController: NavHostController,
    viewModel: FavoritosViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pantalla_favoritos)) },
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                estado.cargando -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                !estado.haySesion -> EstadoVacio(
                    icono = Icons.Filled.Lock,
                    texto = stringResource(R.string.favoritos_sin_sesion),
                    textoBoton = stringResource(R.string.p3_ir_a_login),
                    alHacerClick = { navController.navigate(Routes.Login.route) },
                    modifier = Modifier.align(Alignment.Center)
                )

                estado.museos.isEmpty() -> EstadoVacio(
                    icono = Icons.Filled.FavoriteBorder,
                    texto = stringResource(R.string.favoritos_vacio),
                    textoBoton = stringResource(R.string.favoritos_explorar),
                    alHacerClick = {
                        navController.navigate(Routes.Mapa.route) {
                            popUpTo(Routes.Dashboard.route)
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = stringResource(R.string.favoritos_contador, estado.museos.size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // animateItem: al quitar un favorito, el resto se desliza a su nuevo lugar
                    items(estado.museos, key = { it.id }) { museo ->
                        TarjetaFavorito(
                            museo = museo,
                            alAbrir = { navController.navigate(Routes.DetalleMuseo.crear(museo.id)) },
                            alQuitar = { viewModel.quitar(museo.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaFavorito(museo: Museum, alAbrir: () -> Unit, alQuitar: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().clickable(onClick = alAbrir)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Museum,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(museo.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(
                        text = stringResource(R.string.detalle_ubicacion, museo.ciudad, museo.pais) + " · " + museo.clasificacion.etiqueta,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            IconButton(onClick = alQuitar) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = stringResource(R.string.detalle_quitar_favorito),
                    tint = ROJO_FAVORITO
                )
            }
        }
    }
}

/** Estado vacío con animación de entrada (escala + fundido). */
@Composable
private fun EstadoVacio(
    icono: ImageVector,
    texto: String,
    textoBoton: String,
    alHacerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Empieza oculto y pasa a visible, así la animación se ve al entrar a la pantalla
    val aparicion = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(visibleState = aparicion, enter = scaleIn() + fadeIn(), modifier = modifier) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
            Text(texto, textAlign = TextAlign.Center)
            Button(onClick = alHacerClick) { Text(textoBoton) }
        }
    }
}
