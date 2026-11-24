package com.museo.productos.dto;

/** Una línea del carrito que no se puede comprar, con el stock real del servidor. */
public record ProblemaStock(
        String productoId,
        String nombre,
        int solicitado,
        int disponible,
        MotivoProblema motivo
) {
}
