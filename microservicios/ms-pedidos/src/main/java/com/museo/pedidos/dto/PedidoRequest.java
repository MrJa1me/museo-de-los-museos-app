package com.museo.pedidos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Solicitud de compra que envía la app al confirmar el carrito. */
public record PedidoRequest(
        /** Opcional: si viene vacío, el servidor genera uno (sol_<milisegundos>). */
        @Size(max = 40) String id,
        @NotBlank(message = "El usuario es obligatorio") String userId,
        @NotEmpty(message = "El pedido debe tener al menos un ítem") List<@Valid PedidoItemRequest> items,
        @Min(value = 0, message = "El costo de envío no puede ser negativo") int costoEnvio,
        @NotBlank(message = "El destino es obligatorio") @Size(max = 200) String destino,
        @Size(max = 40) String modalidad,
        Long fechaMillis
) {
}
