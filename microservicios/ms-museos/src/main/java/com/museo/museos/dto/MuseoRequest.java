package com.museo.museos.dto;

import com.museo.museos.model.Clasificacion;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos que envía la app al crear o editar una ficha. Las reglas son las mismas que
 * ValidadorFichaMuseo en la app: se validan en los dos lados (la app para el usuario, el servidor por seguridad).
 */
public record MuseoRequest(
        /** Opcional al crear: si viene vacío, el servidor asigna el siguiente (m11, m12, ...). */
        @Pattern(regexp = "^m\\d{2,}$", message = "El id debe tener el formato m01, m02, ...")
        String id,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 5, max = 120, message = "El nombre debe tener entre 5 y 120 caracteres")
        String nombre,

        @NotBlank(message = "El país es obligatorio")
        @Size(min = 2, max = 60)
        String pais,

        @NotBlank(message = "La ciudad es obligatoria")
        @Size(min = 2, max = 60)
        String ciudad,

        @NotNull(message = "La latitud es obligatoria")
        @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
        @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
        Double latitud,

        @NotNull(message = "La longitud es obligatoria")
        @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
        @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
        Double longitud,

        @NotNull(message = "La clasificación es obligatoria")
        Clasificacion clasificacion,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(min = 20, max = 1000, message = "La descripción debe tener al menos 20 caracteres")
        String descripcion,

        @NotBlank(message = "El enlace es obligatorio")
        @Pattern(regexp = "^https?://\\S+$", message = "El enlace debe empezar con http:// o https://")
        String enlace,

        String proyecto,
        String imagen
) {
}
