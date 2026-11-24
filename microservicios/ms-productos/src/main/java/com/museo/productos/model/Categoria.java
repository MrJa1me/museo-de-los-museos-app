package com.museo.productos.model;

/**
 * Mismos valores que CategoriaProducto en la app móvil.
 * Cada categoría tiene el prefijo que usa el id del producto (c01, p01, m01k, a01).
 */
public enum Categoria {
    CURSO("c", ""),
    PUBLICACION("p", ""),
    MATERIAL_CONSERVACION("m", "k"),
    ARTICULO_MUSEOLOGICO("a", "");

    private final String prefijo;
    private final String sufijo;

    Categoria(String prefijo, String sufijo) {
        this.prefijo = prefijo;
        this.sufijo = sufijo;
    }

    public String getPrefijo() { return prefijo; }
    public String getSufijo() { return sufijo; }
}
