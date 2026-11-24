package com.museo.productos.controller;

import com.museo.productos.dto.DisponibilidadResponse;
import com.museo.productos.dto.MotivoProblema;
import com.museo.productos.dto.ProblemaStock;
import com.museo.productos.exception.ConflictoException;
import com.museo.productos.exception.DatoInvalidoException;
import com.museo.productos.exception.RecursoNoEncontradoException;
import com.museo.productos.model.Categoria;
import com.museo.productos.model.Producto;
import com.museo.productos.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba la capa HTTP (rutas, códigos y JSON) con el servicio simulado. */
@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProductoService servicio;

    private static final String JSON_VALIDO = """
            {"nombre":"Taller de Archivos","categoria":"PUBLICACION","precio":35000,
             "stock":12,"pesoKg":0.5,"descripcion":"Taller práctico"}
            """;

    private static Producto producto(String id, boolean activo) {
        return new Producto(id, "Producto " + id, Categoria.PUBLICACION, 12000, 5, 0.5, "Descripción", activo);
    }

    @Test
    void listar_conFiltros_devuelve200() throws Exception {
        when(servicio.listar(Categoria.PUBLICACION, true)).thenReturn(List.of(producto("p01", true)));
        mvc.perform(get("/api/productos").param("categoria", "PUBLICACION").param("activo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("p01"))
                .andExpect(jsonPath("$[0].pesoKg").value(0.5));
    }

    @Test
    void obtener_inexistente_devuelve404() throws Exception {
        when(servicio.obtener("x99")).thenThrow(new RecursoNoEncontradoException("No existe el producto x99"));
        mvc.perform(get("/api/productos/x99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe el producto x99"));
    }

    @Test
    void crear_valido_devuelve201() throws Exception {
        when(servicio.crear(any())).thenReturn(producto("p03", true));
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("p03"));
    }

    @Test
    void crear_precioCero_devuelve400ConElCampo() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO.replace("35000", "0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.precio").value("El precio debe ser mayor que 0"));
    }

    @Test
    void crear_sinPeso_reglaDelServicio_devuelve400() throws Exception {
        when(servicio.crear(any())).thenThrow(new DatoInvalidoException("pesoKg", "El peso es obligatorio para productos con envío"));
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.pesoKg").exists());
    }

    @Test
    void crear_jsonRoto_devuelve400() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content("{roto"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizar_devuelve200() throws Exception {
        when(servicio.actualizar(eq("p01"), any())).thenReturn(producto("p01", true));
        mvc.perform(put("/api/productos/p01").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isOk());
    }

    @Test
    void cambiarActivo_devuelveElProducto() throws Exception {
        when(servicio.cambiarActivo("a01", false)).thenReturn(producto("a01", false));
        mvc.perform(patch("/api/productos/a01/activo").contentType(MediaType.APPLICATION_JSON).content("{\"activo\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    void cambiarActivo_sinCuerpo_devuelve400() throws Exception {
        mvc.perform(patch("/api/productos/a01/activo").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.activo").exists());
    }

    @Test
    void eliminar_devuelve204() throws Exception {
        mvc.perform(delete("/api/productos/a01")).andExpect(status().isNoContent());
        verify(servicio).eliminar("a01");
    }

    @Test
    void eliminar_inexistente_devuelve404() throws Exception {
        doThrow(new RecursoNoEncontradoException("No existe")).when(servicio).eliminar("x99");
        mvc.perform(delete("/api/productos/x99")).andExpect(status().isNotFound());
    }

    @Test
    void disponibilidad_devuelveLosProblemas() throws Exception {
        when(servicio.verificarDisponibilidad(anyList())).thenReturn(new DisponibilidadResponse(false,
                List.of(new ProblemaStock("p01", "Guía", 3, 2, MotivoProblema.SIN_STOCK))));
        mvc.perform(post("/api/productos/disponibilidad").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productoId\":\"p01\",\"cantidad\":3}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(false))
                .andExpect(jsonPath("$.problemas[0].motivo").value("SIN_STOCK"));
    }

    @Test
    void disponibilidad_cantidadCero_devuelve400() throws Exception {
        mvc.perform(post("/api/productos/disponibilidad").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productoId\":\"p01\",\"cantidad\":0}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void descontarStock_sinStock_devuelve409() throws Exception {
        when(servicio.descontarStock(anyList())).thenThrow(new ConflictoException("Stock insuficiente para \"Guía\": quedan 0"));
        mvc.perform(post("/api/productos/descontar-stock").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productoId\":\"p01\",\"cantidad\":1}]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Stock insuficiente para \"Guía\": quedan 0"));
    }
}
