package com.museo.productos.exception;

/** Se transforma en HTTP 409 (id repetido o stock insuficiente al descontar). */
public class ConflictoException extends RuntimeException {
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
