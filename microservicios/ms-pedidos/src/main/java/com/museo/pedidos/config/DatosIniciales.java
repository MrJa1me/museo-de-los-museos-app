package com.museo.pedidos.config;

import com.museo.pedidos.model.EstadoPedido;
import com.museo.pedidos.model.Pedido;
import com.museo.pedidos.model.PedidoItem;
import com.museo.pedidos.repository.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/** Carga los 2 pedidos ficticios de la app (PEDIDOS_SEMILLA) solo cuando la tabla está vacía. */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final PedidoRepository repositorio;

    public DatosIniciales(PedidoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void run(String... args) {
        if (repositorio.count() > 0) return;
        long ahora = System.currentTimeMillis();
        repositorio.saveAll(List.of(
                pedido("sol_101", "u03", new PedidoItem("p01", "Guía de Museos Virtuales", 1, 12000),
                        "Región Metropolitana", ahora - 86_400_000L),
                pedido("sol_102", "u04", new PedidoItem("m01k", "Kit de guantes y papel libre de ácido", 1, 18000),
                        "Valparaíso", ahora - 43_200_000L)
        ));
        log.info("Se cargaron {} pedidos de prueba", repositorio.count());
    }

    private static Pedido pedido(String id, String userId, PedidoItem item, String destino, long fecha) {
        Pedido pedido = new Pedido();
        pedido.setId(id);
        pedido.setUserId(userId);
        pedido.reemplazarItems(List.of(item));
        int subtotal = item.getCantidad() * item.getPrecioUnitario();
        pedido.setSubtotal(subtotal);
        pedido.setCostoEnvio(3500);
        pedido.setTotal(subtotal + 3500);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setDestino(destino);
        pedido.setModalidad("Estándar");
        pedido.setFechaMillis(fecha);
        return pedido;
    }
}
