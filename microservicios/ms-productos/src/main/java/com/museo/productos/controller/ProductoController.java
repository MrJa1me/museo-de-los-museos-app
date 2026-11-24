package com.museo.productos.controller;

import com.museo.productos.dto.CambioActivoRequest;
import com.museo.productos.dto.DisponibilidadResponse;
import com.museo.productos.dto.ProductoRequest;
import com.museo.productos.dto.StockRequest;
import com.museo.productos.model.Categoria;
import com.museo.productos.model.Producto;
import com.museo.productos.service.ProductoService;
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

/** Endpoints REST del catálogo de productos y cursos. */
@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "CRUD del catálogo, activación y control de stock")
public class ProductoController {

    private final ProductoService servicio;

    public ProductoController(ProductoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Lista el catálogo. Filtros opcionales: ?categoria=CURSO y ?activo=true")
    public List<Producto> listar(@RequestParam(required = false) Categoria categoria,
                                 @RequestParam(required = false) Boolean activo) {
        return servicio.listar(categoria, activo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un producto por id")
    public Producto obtener(@PathVariable String id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @Operation(summary = "Crea un producto (el servidor asigna el id según la categoría)")
    public ResponseEntity<Producto> crear(@Valid @RequestBody ProductoRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(datos));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita un producto (no cambia si está activo)")
    public Producto actualizar(@PathVariable String id, @Valid @RequestBody ProductoRequest datos) {
        return servicio.actualizar(id, datos);
    }

    @PatchMapping("/{id}/activo")
    @Operation(summary = "Activa o desactiva un producto: {\"activo\": false}")
    public Producto cambiarActivo(@PathVariable String id, @Valid @RequestBody CambioActivoRequest cambio) {
        return servicio.cambiarActivo(id, cambio.activo());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un producto (la app usa desactivar; este endpoint queda para administración)")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/disponibilidad")
    @Operation(summary = "Revisa, sin modificar nada, si las líneas del carrito tienen stock y están activas")
    public DisponibilidadResponse disponibilidad(@Valid @RequestBody StockRequest solicitud) {
        return servicio.verificarDisponibilidad(solicitud.items());
    }

    @PostMapping("/descontar-stock")
    @Operation(summary = "Descuenta el stock de una compra confirmada (todo o nada; 409 si no alcanza)")
    public List<Producto> descontarStock(@Valid @RequestBody StockRequest solicitud) {
        return servicio.descontarStock(solicitud.items());
    }
}
