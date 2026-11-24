package com.museo.productos.dto;

import com.museo.productos.model.Categoria;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos que envía la app al crear o editar un producto. Las reglas son las mismas que
 * Validadores (validarNombreProducto, validarPrecio, validarStock, validarPeso) en la app:
 * la app valida para ayudar al usuario y el servidor vuelve a validar por seguridad.
 */
public record ProductoRequest(
        /** Opcional al crear: si viene vacío, el servidor asigna el siguiente según la categoría (c05, p03...). */
        @Pattern(regexp = "^[cpma]\\d{2,}k?$", message = "El id debe tener el formato c01, p01, m01k o a01")
        String id,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres")
        String nombre,

        @NotNull(message = "La categoría es obligatoria")
        Categoria categoria,

        @NotNull(message = "El precio es obligatorio")
        @Min(value = 1, message = "El precio debe ser mayor que 0")
        @Max(value = 9_999_999, message = "El precio es demasiado alto")
        Integer precio,

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,

        /** Obligatorio salvo en los cursos (la regla está en ProductoService porque depende de la categoría). */
        @DecimalMin(value = "0.01", message = "El peso debe ser mayor que 0")
        @DecimalMax(value = "30.0", message = "El peso máximo es 30 kg")
        Double pesoKg,

        @Size(max = 500, message = "La descripción admite hasta 500 caracteres")
        String descripcion
) {
}
