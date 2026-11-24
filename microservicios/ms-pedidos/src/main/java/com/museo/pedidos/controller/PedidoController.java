package com.museo.pedidos.controller;

import com.museo.pedidos.dto.ActualizarDestinoRequest;
import com.museo.pedidos.dto.CambioEstadoRequest;
import com.museo.pedidos.dto.PedidoRequest;
import com.museo.pedidos.model.EstadoPedido;
import com.museo.pedidos.model.Pedido;
import com.museo.pedidos.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Endpoints REST de las solicitudes de compra. */
@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Solicitudes de compra del catálogo y gestión de estados")
public class PedidoController {

    private final PedidoService servicio;

    public PedidoController(PedidoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Lista pedidos. ?userId=u03 los de un cliente, ?estado=PENDIENTE por estado")
    public List<Pedido> listar(@RequestParam(required = false) String userId,
                               @RequestParam(required = false) EstadoPedido estado) {
        return servicio.listar(userId, estado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un pedido con sus ítems")
    public Pedido obtener(@PathVariable String id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @Operation(summary = "Crea una solicitud de compra (queda PENDIENTE)")
    public ResponseEntity<Pedido> crear(@Valid @RequestBody PedidoRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(datos));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cambia destino y modalidad de un pedido pendiente")
    public Pedido actualizarDestino(@PathVariable String id, @Valid @RequestBody ActualizarDestinoRequest datos) {
        return servicio.actualizarDestino(id, datos);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "El Administrador avanza el estado al siguiente paso")
    public Pedido cambiarEstado(@PathVariable String id, @Valid @RequestBody CambioEstadoRequest datos) {
        return servicio.cambiarEstado(id, datos.estado());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancela (elimina) un pedido que aún no fue enviado")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
