package com.museo.pedidos.service;

import com.museo.pedidos.dto.ActualizarDestinoRequest;
import com.museo.pedidos.dto.PedidoItemRequest;
import com.museo.pedidos.dto.PedidoRequest;
import com.museo.pedidos.exception.ConflictoException;
import com.museo.pedidos.exception.RecursoNoEncontradoException;
import com.museo.pedidos.model.EstadoPedido;
import com.museo.pedidos.model.Pedido;
import com.museo.pedidos.repository.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Prueba las reglas de negocio de los pedidos con el repositorio simulado (Mockito). */
@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository repositorio;

    @InjectMocks
    private PedidoService servicio;

    private static PedidoRequest solicitud(String id) {
        return new PedidoRequest(id, "u03",
                List.of(new PedidoItemRequest("p01", "Guía", 2, 12000), new PedidoItemRequest("a01", "Bolso", 1, 8000)),
                3500, "Región Metropolitana", "Estándar", null);
    }

    private static Pedido pedido(String id, EstadoPedido estado) {
        Pedido p = new Pedido();
        p.setId(id);
        p.setUserId("u03");
        p.setEstado(estado);
        p.setDestino("Valparaíso");
        return p;
    }

    @Test
    void listar_porUsuario() {
        when(repositorio.findByUserIdOrderByFechaMillisDesc("u03")).thenReturn(List.of(pedido("sol_1", EstadoPedido.PENDIENTE)));
        assertThat(servicio.listar("u03", null)).hasSize(1);
    }

    @Test
    void listar_porEstado() {
        when(repositorio.findByEstadoOrderByFechaMillisDesc(EstadoPedido.PENDIENTE)).thenReturn(List.of());
        assertThat(servicio.listar(null, EstadoPedido.PENDIENTE)).isEmpty();
    }

    @Test
    void listar_todos() {
        when(repositorio.findAllByOrderByFechaMillisDesc()).thenReturn(List.of(pedido("a", EstadoPedido.PENDIENTE), pedido("b", EstadoPedido.ENVIADO)));
        assertThat(servicio.listar("  ", null)).hasSize(2);
    }

    @Test
    void crear_recalculaSubtotalYTotalEnElServidor() {
        when(repositorio.existsById("sol_9")).thenReturn(false);
        when(repositorio.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        Pedido creado = servicio.crear(solicitud("sol_9"));

        assertThat(creado.getSubtotal()).isEqualTo(2 * 12000 + 8000);
        assertThat(creado.getTotal()).isEqualTo(32000 + 3500);
        assertThat(creado.getEstado()).isEqualTo(EstadoPedido.PENDIENTE);
        assertThat(creado.getItems()).hasSize(2).allMatch(item -> item.getPedido() == creado);
    }

    @Test
    void crear_sinId_generaUno() {
        when(repositorio.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));
        assertThat(servicio.crear(solicitud(null)).getId()).startsWith("sol_");
    }

    @Test
    void crear_idRepetido_lanzaConflicto() {
        when(repositorio.existsById("sol_101")).thenReturn(true);
        assertThatThrownBy(() -> servicio.crear(solicitud("sol_101"))).isInstanceOf(ConflictoException.class);
    }

    @Test
    void cambiarEstado_soloAvanzaUnPaso() {
        when(repositorio.findById("sol_1")).thenReturn(Optional.of(pedido("sol_1", EstadoPedido.PENDIENTE)));
        when(repositorio.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));
        assertThat(servicio.cambiarEstado("sol_1", EstadoPedido.CONFIRMADO).getEstado()).isEqualTo(EstadoPedido.CONFIRMADO);
    }

    @Test
    void cambiarEstado_saltandoPasos_lanzaConflicto() {
        when(repositorio.findById("sol_1")).thenReturn(Optional.of(pedido("sol_1", EstadoPedido.PENDIENTE)));
        assertThatThrownBy(() -> servicio.cambiarEstado("sol_1", EstadoPedido.ENTREGADO)).isInstanceOf(ConflictoException.class);
    }

    @Test
    void actualizarDestino_pendiente_cambiaDestino() {
        when(repositorio.findById("sol_1")).thenReturn(Optional.of(pedido("sol_1", EstadoPedido.PENDIENTE)));
        when(repositorio.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));
        Pedido p = servicio.actualizarDestino("sol_1", new ActualizarDestinoRequest(" Antofagasta ", "Express"));
        assertThat(p.getDestino()).isEqualTo("Antofagasta");
        assertThat(p.getModalidad()).isEqualTo("Express");
    }

    @Test
    void actualizarDestino_confirmado_lanzaConflicto() {
        when(repositorio.findById("sol_1")).thenReturn(Optional.of(pedido("sol_1", EstadoPedido.CONFIRMADO)));
        assertThatThrownBy(() -> servicio.actualizarDestino("sol_1", new ActualizarDestinoRequest("X", null)))
                .isInstanceOf(ConflictoException.class);
    }

    @Test
    void eliminar_pendiente_borra() {
        Pedido p = pedido("sol_1", EstadoPedido.PENDIENTE);
        when(repositorio.findById("sol_1")).thenReturn(Optional.of(p));
        servicio.eliminar("sol_1");
        verify(repositorio).delete(p);
    }

    @Test
    void eliminar_enviado_noSePuedeCancelar() {
        when(repositorio.findById("sol_1")).thenReturn(Optional.of(pedido("sol_1", EstadoPedido.ENVIADO)));
        assertThatThrownBy(() -> servicio.eliminar("sol_1")).isInstanceOf(ConflictoException.class);
        verify(repositorio, never()).delete(any());
    }

    @Test
    void obtener_inexistente_lanza404() {
        when(repositorio.findById("x")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> servicio.obtener("x")).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void estadoSiguiente_terminaEnEntregado() {
        assertThat(EstadoPedido.ENVIADO.siguiente()).isEqualTo(EstadoPedido.ENTREGADO);
        assertThat(EstadoPedido.ENTREGADO.siguiente()).isNull();
    }
}
