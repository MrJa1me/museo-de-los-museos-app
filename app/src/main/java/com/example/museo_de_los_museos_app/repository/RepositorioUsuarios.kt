package com.example.museo_de_los_museos_app.repository

import android.content.Context

/**
 * Acceso a los usuarios con la misma forma que el resto de repositorios:  Repositorios.usuarios
 *
 * Está definido como extensión en este archivo (y no dentro de Repositorios.kt) para que P1 pueda agregar
 * los usuarios sin tocar Repositorios.kt, que también editan P2 y los demás.
 * Para usarlo en otra carpeta:  import com.example.museo_de_los_museos_app.repository.usuarios
 */
private object AlmacenUsuarios {
    lateinit var repositorio: UserRepository
}

val Repositorios.usuarios: UserRepository
    get() = AlmacenUsuarios.repositorio

/** MainActivity lo llama una vez al iniciar, antes de mostrar cualquier pantalla. */
fun Repositorios.inicializarUsuarios(context: Context) {
    AlmacenUsuarios.repositorio = RoomUserRepository(baseDatos.usuarioDao())
}
