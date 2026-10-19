package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.model.User
import org.json.JSONArray
import org.json.JSONObject

/** Convierte la lista de usuarios a texto JSON para guardarla en DataStore (org.json viene incluido en Android). */
internal fun List<User>.aJson(): String {
    val arreglo = JSONArray()
    forEach { usuario ->
        val objeto = JSONObject()
        objeto.put("id", usuario.id)
        objeto.put("nombre", usuario.nombre)
        objeto.put("correo", usuario.correo)
        objeto.put("contrasena", usuario.contrasena)
        objeto.put("rol", usuario.rol.name)
        objeto.put("avatarUri", usuario.avatarUri ?: JSONObject.NULL)
        arreglo.put(objeto)
    }
    return arreglo.toString()
}

internal fun usuariosDesdeJson(json: String): List<User> {
    val arreglo = JSONArray(json)
    return List(arreglo.length()) { indice ->
        val objeto = arreglo.getJSONObject(indice)
        User(
            id = objeto.getString("id"),
            nombre = objeto.getString("nombre"),
            correo = objeto.getString("correo"),
            contrasena = objeto.getString("contrasena"),
            rol = Rol.valueOf(objeto.getString("rol")),
            avatarUri = if (objeto.isNull("avatarUri")) null else objeto.getString("avatarUri")
        )
    }
}
