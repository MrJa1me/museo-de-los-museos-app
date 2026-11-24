package com.museo.productos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** Cuerpo de POST /api/productos/disponibilidad y POST /api/productos/descontar-stock. */
public record StockRequest(
        @NotEmpty(message = "Debe venir al menos un producto")
        List<@Valid ItemStockRequest> items
) {
}
