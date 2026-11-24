package com.museo.museos.exception;

/** Se transforma en HTTP 409 (por ejemplo, crear una ficha con un id que ya existe). */
public class ConflictoException extends RuntimeException {
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
