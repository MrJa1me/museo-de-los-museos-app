package com.museo.productos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Una línea del carrito: qué producto y cuántas unidades. */
public record ItemStockRequest(
        @NotBlank(message = "El id del producto es obligatorio")
        String productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        Integer cantidad
) {
}
