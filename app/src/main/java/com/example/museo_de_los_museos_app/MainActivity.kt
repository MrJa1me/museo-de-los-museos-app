package com.example.museo_de_los_museos_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.navigation.AppNavHost
import com.example.museo_de_los_museos_app.repository.SeedData
import com.example.museo_de_los_museos_app.repository.SessionManager
import com.example.museo_de_los_museos_app.ui.theme.MuseodelosmuseosappTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // osmdroid (mapa de P3): identifica la app ante los servidores de OpenStreetMap
        Configuration.getInstance().userAgentValue = packageName

        // TEMPORAL (P1): sesión de prueba hasta que exista la pantalla de Login.
        // Cambia Rol.CLIENTE por Rol.ADMINISTRADOR o Rol.INVESTIGADOR para probar otros perfiles.
        // P1 elimina este bloque cuando Login guarde la sesión de verdad.
        if (SessionManager.userId == null) {
            SessionManager.iniciarSesion(SeedData.usuarios.first { it.rol == Rol.CLIENTE })
        }

        enableEdgeToEdge()
        setContent {
            MuseodelosmuseosappTheme {
                AppNavHost()
            }
        }
    }
}
