package com.museo.pedidos.model;

/** Flujo de una solicitud: Pendiente → Confirmado → Enviado → Entregado (igual que en la app). */
public enum EstadoPedido {
    PENDIENTE, CONFIRMADO, ENVIADO, ENTREGADO;

    /** Siguiente estado del flujo, o null si ya está entregado. */
    public EstadoPedido siguiente() {
        EstadoPedido[] estados = values();
        return ordinal() + 1 < estados.length ? estados[ordinal() + 1] : null;
    }
}
