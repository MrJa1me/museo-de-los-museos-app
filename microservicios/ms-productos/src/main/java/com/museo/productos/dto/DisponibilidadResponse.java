package com.museo.productos.dto;

import java.util.List;

/** Respuesta de POST /api/productos/disponibilidad. disponible = true si no hay ningún problema. */
public record DisponibilidadResponse(
        boolean disponible,
        List<ProblemaStock> problemas
) {
}
