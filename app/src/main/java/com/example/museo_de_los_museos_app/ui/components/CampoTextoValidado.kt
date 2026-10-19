package com.example.museo_de_los_museos_app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.museo_de_los_museos_app.R

/**
 * Campo de texto reutilizable con validación visual (IE 2.1.2):
 * - borde y mensaje rojo bajo el campo cuando [mensajeError] no es null,
 * - ícono de error, ícono de check cuando [mostrarValido] es true, y ojo para mostrar/ocultar contraseña.
 *
 * No valida nada por sí mismo: solo MUESTRA el mensaje que le entrega el ViewModel.
 */
@Composable
fun CampoTextoValidado(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    iconoInicial: ImageVector,
    modifier: Modifier = Modifier,
    mensajeError: String? = null,
    esContrasena: Boolean = false,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    accionIme: ImeAction = ImeAction.Next,
    mostrarValido: Boolean = false
) {
    var contrasenaVisible by rememberSaveable { mutableStateOf(false) }
    val hayError = mensajeError != null

    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        modifier = modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        singleLine = true,
        isError = hayError,
        leadingIcon = { Icon(imageVector = iconoInicial, contentDescription = null) },
        trailingIcon = {
            when {
                esContrasena -> IconButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                    Icon(
                        imageVector = if (contrasenaVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = stringResource(
                            if (contrasenaVisible) R.string.cd_ocultar_contrasena else R.string.cd_mostrar_contrasena
                        )
                    )
                }
                hayError -> Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                mostrarValido -> Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        supportingText = { if (mensajeError != null) Text(mensajeError) },
        visualTransformation = if (esContrasena && !contrasenaVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (esContrasena) KeyboardType.Password else tipoTeclado,
            imeAction = accionIme
        )
    )
}
