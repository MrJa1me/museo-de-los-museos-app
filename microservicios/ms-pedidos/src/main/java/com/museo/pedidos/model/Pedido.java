package com.museo.pedidos.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/** Tabla "pedidos": una solicitud de compra de un usuario. */
@Entity
@Table(name = "pedidos")
public class Pedido {

    /** Mismo formato que la app: sol_101, sol_<fecha>... */
    @Id
    @Column(length = 40)
    private String id;

    /** Id del usuario de la app (u03, u04...). Los usuarios viven en la app, aquí solo se guarda la referencia. */
    @Column(nullable = false, length = 40)
    private String userId;

    /** Relación 1 a N: al guardar o borrar un pedido se guardan o borran sus ítems (cascade + orphanRemoval). */
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PedidoItem> items = new ArrayList<>();

    @Column(nullable = false)
    private int subtotal;

    @Column(nullable = false)
    private int costoEnvio;

    @Column(nullable = false)
    private int total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Column(nullable = false, length = 200)
    private String destino = "";

    @Column(nullable = false, length = 40)
    private String modalidad = "";

    @Column(nullable = false)
    private long fechaMillis;

    public Pedido() {
    }

    /** Reemplaza los ítems manteniendo la relación en los dos sentidos (lo exige JPA). */
    public void reemplazarItems(List<PedidoItem> nuevos) {
        items.clear();
        for (PedidoItem item : nuevos) {
            item.setPedido(this);
            items.add(item);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<PedidoItem> getItems() { return items; }
    public int getSubtotal() { return subtotal; }
    public void setSubtotal(int subtotal) { this.subtotal = subtotal; }
    public int getCostoEnvio() { return costoEnvio; }
    public void setCostoEnvio(int costoEnvio) { this.costoEnvio = costoEnvio; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }
    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }
    public long getFechaMillis() { return fechaMillis; }
    public void setFechaMillis(long fechaMillis) { this.fechaMillis = fechaMillis; }
}
