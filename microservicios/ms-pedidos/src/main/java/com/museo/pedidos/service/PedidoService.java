package com.museo.pedidos.service;

import com.museo.pedidos.dto.ActualizarDestinoRequest;
import com.museo.pedidos.dto.PedidoItemRequest;
import com.museo.pedidos.dto.PedidoRequest;
import com.museo.pedidos.exception.ConflictoException;
import com.museo.pedidos.exception.RecursoNoEncontradoException;
import com.museo.pedidos.model.EstadoPedido;
import com.museo.pedidos.model.Pedido;
import com.museo.pedidos.model.PedidoItem;
import com.museo.pedidos.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Reglas de negocio de las solicitudes de compra. */
@Service
public class PedidoService {

    private final PedidoRepository repositorio;

    public PedidoService(PedidoRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** Filtra por usuario (lo que ve un Cliente) o por estado (lo que gestiona el Administrador). */
    public List<Pedido> listar(String userId, EstadoPedido estado) {
        if (userId != null && !userId.isBlank()) return repositorio.findByUserIdOrderByFechaMillisDesc(userId);
        if (estado != null) return repositorio.findByEstadoOrderByFechaMillisDesc(estado);
        return repositorio.findAllByOrderByFechaMillisDesc();
    }

    public Pedido obtener(String id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el pedido " + id));
    }

    /** El servidor recalcula subtotal y total: no confía en los montos que manda el cliente. */
    @Transactional
    public Pedido crear(PedidoRequest datos) {
        long ahora = System.currentTimeMillis();
        String id = (datos.id() == null || datos.id().isBlank()) ? "sol_" + ahora : datos.id();
        if (repositorio.existsById(id)) {
            throw new ConflictoException("Ya existe un pedido con el id " + id);
        }
        Pedido pedido = new Pedido();
        pedido.setId(id);
        pedido.setUserId(datos.userId());
        pedido.reemplazarItems(datos.items().stream().map(PedidoService::aItem).toList());
        int subtotal = calcularSubtotal(datos.items());
        pedido.setSubtotal(subtotal);
        pedido.setCostoEnvio(datos.costoEnvio());
        pedido.setTotal(subtotal + datos.costoEnvio());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setDestino(datos.destino().trim());
        pedido.setModalidad(datos.modalidad() == null ? "" : datos.modalidad().trim());
        pedido.setFechaMillis(datos.fechaMillis() != null ? datos.fechaMillis() : ahora);
        return repositorio.save(pedido);
    }

    /** Solo se puede cambiar el destino mientras el pedido no ha sido confirmado. */
    @Transactional
    public Pedido actualizarDestino(String id, ActualizarDestinoRequest datos) {
        Pedido pedido = obtener(id);
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new ConflictoException("Solo se puede modificar un pedido pendiente");
        }
        pedido.setDestino(datos.destino().trim());
        if (datos.modalidad() != null) pedido.setModalidad(datos.modalidad().trim());
        return repositorio.save(pedido);
    }

    /** El estado solo avanza un paso: Pendiente → Confirmado → Enviado → Entregado. */
    @Transactional
    public Pedido cambiarEstado(String id, EstadoPedido nuevo) {
        Pedido pedido = obtener(id);
        if (pedido.getEstado().siguiente() != nuevo) {
            throw new ConflictoException("No se puede pasar de " + pedido.getEstado() + " a " + nuevo);
        }
        pedido.setEstado(nuevo);
        return repositorio.save(pedido);
    }

    /** Cancelar = eliminar. Solo se permite antes de que el pedido sea enviado. */
    @Transactional
    public void eliminar(String id) {
        Pedido pedido = obtener(id);
        if (pedido.getEstado() == EstadoPedido.ENVIADO || pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new ConflictoException("No se puede cancelar un pedido " + pedido.getEstado());
        }
        repositorio.delete(pedido);
    }

    static int calcularSubtotal(List<PedidoItemRequest> items) {
        return items.stream().mapToInt(i -> i.cantidad() * i.precioUnitario()).sum();
    }

    private static PedidoItem aItem(PedidoItemRequest datos) {
        return new PedidoItem(datos.productoId(), datos.nombre().trim(), datos.cantidad(), datos.precioUnitario());
    }
}
