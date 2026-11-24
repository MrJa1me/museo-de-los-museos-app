package com.museo.productos.dto;

import jakarta.validation.constraints.NotNull;

/** Cuerpo de PATCH /api/productos/{id}/activo: {"activo": false} desactiva el producto. */
public record CambioActivoRequest(
        @NotNull(message = "Indica si el producto queda activo o no")
        Boolean activo
) {
}
