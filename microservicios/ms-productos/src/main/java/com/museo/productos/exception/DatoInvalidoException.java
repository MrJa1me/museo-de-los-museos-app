package com.museo.productos.exception;

/**
 * Regla de validación que depende de varios campos (ej. "el peso es obligatorio si no es curso").
 * Se transforma en HTTP 400 con el mismo formato que los errores de @Valid.
 */
public class DatoInvalidoException extends RuntimeException {

    private final String campo;

    public DatoInvalidoException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() { return campo; }
}
