package com.museo.productos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Tabla "productos": una fila por curso o producto del catálogo.
 * - Cursos: stock son los cupos y pesoKg es null (no tienen envío).
 * - Productos físicos: pesoKg se usa en la calculadora de envío de la app.
 */
@Entity
@Table(name = "productos")
public class Producto {

    /** Mismo formato que la app: c01 (curso), p01 (publicación), m01k (material), a01 (artículo). */
    @Id
    @Column(length = 10)
    private String id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Categoria categoria;

    /** Precio ficticio en pesos chilenos. */
    @Column(nullable = false)
    private int precio;

    /** Unidades disponibles (o cupos si es un curso). 0 = agotado. */
    @Column(nullable = false)
    private int stock;

    /** null en los cursos. */
    private Double pesoKg;

    @Column(nullable = false, length = 500)
    private String descripcion = "";

    /** Desactivar no borra: el producto deja de mostrarse a los Clientes. */
    @Column(nullable = false)
    private boolean activo = true;

    public Producto() {
    }

    public Producto(String id, String nombre, Categoria categoria, int precio, int stock,
                    Double pesoKg, String descripcion, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
        this.pesoKg = pesoKg;
        this.descripcion = descripcion;
        this.activo = activo;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public int getPrecio() { return precio; }
    public void setPrecio(int precio) { this.precio = precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public Double getPesoKg() { return pesoKg; }
    public void setPesoKg(Double pesoKg) { this.pesoKg = pesoKg; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
