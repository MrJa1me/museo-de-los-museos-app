package com.museo.pedidos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cambio de destino o modalidad de una solicitud aún pendiente. */
public record ActualizarDestinoRequest(
        @NotBlank(message = "El destino es obligatorio") @Size(max = 200) String destino,
        @Size(max = 40) String modalidad
) {
}
