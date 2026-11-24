package com.example.museo_de_los_museos_app.repository

/** De dónde vienen los datos que se muestran. La UI muestra un aviso cuando no hay conexión. */
enum class EstadoConexion {
    /** Todavía no se ha intentado contactar al microservicio. */
    SIN_VERIFICAR,
    /** La última llamada al microservicio funcionó: los datos son los del servidor. */
    CONECTADO,
    /** El microservicio no respondió: se muestra la copia guardada en Room. */
    SIN_CONEXION
}
