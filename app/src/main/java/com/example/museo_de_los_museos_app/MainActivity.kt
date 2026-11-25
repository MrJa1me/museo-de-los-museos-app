package com.example.museo_de_los_museos_app

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
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

    /** Resultado del permiso de red local: si se rechaza, la app muestra «Sin conexión» y usa sus datos guardados. */
    private val lanzadorRedLocal = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

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

        pedirPermisoRedLocal()

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

    /**
     * Desde Android 17 (API 37) una app que apunta al SDK 37 no puede llegar a direcciones de red local
     * (como los microservicios en 10.0.2.2 o en la IP del PC) sin este permiso («Dispositivos cercanos»).
     * Las conexiones fallan con timeout. En versiones anteriores no existe y no se pide.
     */
    private fun pedirPermisoRedLocal() {
        if (Build.VERSION.SDK_INT < 37) return
        val concedido = ContextCompat.checkSelfPermission(this, PERMISO_RED_LOCAL) == PackageManager.PERMISSION_GRANTED
        if (!concedido) lanzadorRedLocal.launch(PERMISO_RED_LOCAL)
    }

    private companion object {
        const val PERMISO_RED_LOCAL = "android.permission.ACCESS_LOCAL_NETWORK"
    }
}
