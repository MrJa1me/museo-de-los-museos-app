package com.museo.museos.controller;

import com.museo.museos.dto.MuseoRequest;
import com.museo.museos.model.Museo;
import com.museo.museos.service.MuseoService;
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

/** Endpoints REST de las fichas de museos virtuales. */
@RestController
@RequestMapping("/api/museos")
@Tag(name = "Museos", description = "CRUD de fichas de museos virtuales y aprobación del Administrador")
public class MuseoController {

    private final MuseoService servicio;

    public MuseoController(MuseoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Lista las fichas. ?publicado=true solo las aprobadas, ?publicado=false las pendientes")
    public List<Museo> listar(@RequestParam(required = false) Boolean publicado) {
        return servicio.listar(publicado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una ficha por id")
    public Museo obtener(@PathVariable String id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @Operation(summary = "Crea una ficha nueva (queda pendiente de aprobación)")
    public ResponseEntity<Museo> crear(@Valid @RequestBody MuseoRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(datos));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza una ficha existente")
    public Museo actualizar(@PathVariable String id, @Valid @RequestBody MuseoRequest datos) {
        return servicio.actualizar(id, datos);
    }

    @PatchMapping("/{id}/aprobar")
    @Operation(summary = "El Administrador publica una ficha pendiente")
    public Museo aprobar(@PathVariable String id) {
        return servicio.aprobar(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina una ficha")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
