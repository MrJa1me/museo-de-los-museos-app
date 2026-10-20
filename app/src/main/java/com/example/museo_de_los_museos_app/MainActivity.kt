package com.example.museo_de_los_museos_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.museo_de_los_museos_app.navigation.AppNavHost
import com.example.museo_de_los_museos_app.repository.Repositorios
import com.example.museo_de_los_museos_app.repository.inicializarP3
import com.example.museo_de_los_museos_app.repository.inicializarUsuarios
import com.example.museo_de_los_museos_app.repository.Repositorios
import com.example.museo_de_los_museos_app.repository.inicializarUsuarios
import com.example.museo_de_los_museos_app.repository.SessionManager
import com.example.museo_de_los_museos_app.ui.theme.MuseodelosmuseosappTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // osmdroid (mapa de P3): identifica la app ante los servidores de OpenStreetMap
        Configuration.getInstance().userAgentValue = packageName


        Repositorios.inicializarBaseDatos(this)
        Repositorios.inicializarUsuarios(this)
        SessionManager.init(this)
        SessionManager.restaurar()

        // Favoritos e historial QR por usuario (P3)
        Repositorios.inicializarP3(this)

        enableEdgeToEdge()
        setContent {
            MuseodelosmuseosappTheme {
                AppNavHost()
            }
        }
    }
}
