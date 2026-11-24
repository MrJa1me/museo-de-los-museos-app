package com.museo.pedidos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PedidoItemRequest(
        @NotBlank(message = "El producto es obligatorio") String productoId,
        @NotBlank(message = "El nombre del producto es obligatorio") String nombre,
        @Min(value = 1, message = "La cantidad mínima es 1") int cantidad,
        @Min(value = 0, message = "El precio no puede ser negativo") int precioUnitario
) {
}
