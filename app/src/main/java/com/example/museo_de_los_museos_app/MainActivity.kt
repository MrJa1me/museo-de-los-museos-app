package com.example.museo_de_los_museos_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.museo_de_los_museos_app.model.Preferencias
import com.example.museo_de_los_museos_app.navigation.AppNavHost
import com.example.museo_de_los_museos_app.repository.Repositorios
import com.example.museo_de_los_museos_app.repository.SessionManager
import com.example.museo_de_los_museos_app.repository.inicializarFavoritosQr
import com.example.museo_de_los_museos_app.repository.inicializarPreferencias
import com.example.museo_de_los_museos_app.repository.inicializarUsuarios
import com.example.museo_de_los_museos_app.repository.preferencias
import com.example.museo_de_los_museos_app.ui.theme.MuseodelosmuseosappTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // osmdroid (mapa de P3): identifica la app ante los servidores de OpenStreetMap
        Configuration.getInstance().userAgentValue = "MuseoDeLosMuseos/1.0"

        Repositorios.inicializarBaseDatos(this)
        Repositorios.inicializarUsuarios(this)
        Repositorios.inicializarPreferencias()
        SessionManager.init(this)
        SessionManager.restaurar()

        // Favoritos e historial QR por usuario (P3)
        Repositorios.inicializarFavoritosQr(this)

        enableEdgeToEdge()
        setContent {
            // Usuario con sesión (o null). Cuando cambia, se vuelve a dibujar.
            val usuario by SessionManager.usuarioActual.collectAsState()
            val idUsuario = usuario?.id
            // Preferencias del usuario activo, que se actualizan solas al cambiar un switch en Perfil.
            val flujo: Flow<Preferencias?> = remember(idUsuario) {
                if (idUsuario == null) flowOf(null) else Repositorios.preferencias.observar(idUsuario)
            }
            val preferencias by flujo.collectAsState(initial = null)

            MuseodelosmuseosappTheme(textoGrande = preferencias?.textoGrande == true) {
                AppNavHost()
            }
        }
    }
}
