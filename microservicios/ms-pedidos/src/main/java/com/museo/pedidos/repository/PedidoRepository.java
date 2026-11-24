package com.museo.pedidos.repository;

import com.museo.pedidos.model.EstadoPedido;
import com.museo.pedidos.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, String> {

    List<Pedido> findAllByOrderByFechaMillisDesc();

    List<Pedido> findByUserIdOrderByFechaMillisDesc(String userId);

    List<Pedido> findByEstadoOrderByFechaMillisDesc(EstadoPedido estado);
}
