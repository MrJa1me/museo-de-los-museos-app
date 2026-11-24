package com.museo.pedidos.dto;

import com.museo.pedidos.model.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoRequest(@NotNull(message = "El estado es obligatorio") EstadoPedido estado) {
}
