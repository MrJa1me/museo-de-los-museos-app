package com.museo.museos.exception;

/** Se transforma en HTTP 404 en GlobalExceptionHandler. */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
