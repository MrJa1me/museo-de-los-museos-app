package com.museo.museos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Tabla "museos": una fila por ficha de museo virtual. */
@Entity
@Table(name = "museos")
public class Museo {

    /** Mismo formato que la app: m01, m02, ... */
    @Id
    @Column(length = 10)
    private String id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String pais;

    @Column(nullable = false, length = 60)
    private String ciudad;

    @Column(nullable = false)
    private double latitud;

    @Column(nullable = false)
    private double longitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Clasificacion clasificacion;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Column(nullable = false, length = 300)
    private String enlace;

    @Column(nullable = false, length = 80)
    private String proyecto = "Mapa Iberoamérica 2026";

    /** Nombre del drawable en la app (sin extensión) o URI de la galería; vacío = imagen genérica. */
    @Column(nullable = false, length = 500)
    private String imagen = "";

    /** Las fichas nuevas quedan pendientes hasta que el Administrador las aprueba. */
    @Column(nullable = false)
    private boolean publicado = false;

    public Museo() {
    }

    public Museo(String id, String nombre, String pais, String ciudad, double latitud, double longitud,
                 Clasificacion clasificacion, String descripcion, String enlace, String imagen, boolean publicado) {
        this.id = id;
        this.nombre = nombre;
        this.pais = pais;
        this.ciudad = ciudad;
        this.latitud = latitud;
        this.longitud = longitud;
        this.clasificacion = clasificacion;
        this.descripcion = descripcion;
        this.enlace = enlace;
        this.imagen = imagen;
        this.publicado = publicado;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
    public Clasificacion getClasificacion() { return clasificacion; }
    public void setClasificacion(Clasificacion clasificacion) { this.clasificacion = clasificacion; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEnlace() { return enlace; }
    public void setEnlace(String enlace) { this.enlace = enlace; }
    public String getProyecto() { return proyecto; }
    public void setProyecto(String proyecto) { this.proyecto = proyecto; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public boolean isPublicado() { return publicado; }
    public void setPublicado(boolean publicado) { this.publicado = publicado; }
}
