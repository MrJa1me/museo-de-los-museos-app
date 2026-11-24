package com.museo.pedidos.controller;

import com.museo.pedidos.exception.ConflictoException;
import com.museo.pedidos.model.EstadoPedido;
import com.museo.pedidos.model.Pedido;
import com.museo.pedidos.service.PedidoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PedidoService servicio;

    private static final String JSON_VALIDO = """
            {"userId":"u03","costoEnvio":3500,"destino":"Región Metropolitana","modalidad":"Estándar",
             "items":[{"productoId":"p01","nombre":"Guía","cantidad":1,"precioUnitario":12000}]}
            """;

    private static Pedido pedido(String id, EstadoPedido estado) {
        Pedido p = new Pedido();
        p.setId(id);
        p.setUserId("u03");
        p.setEstado(estado);
        return p;
    }

    @Test
    void listar_porUsuario_devuelve200() throws Exception {
        when(servicio.listar("u03", null)).thenReturn(List.of(pedido("sol_101", EstadoPedido.PENDIENTE)));
        mvc.perform(get("/api/pedidos").param("userId", "u03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("sol_101"))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    void obtener_devuelve200() throws Exception {
        when(servicio.obtener("sol_101")).thenReturn(pedido("sol_101", EstadoPedido.PENDIENTE));
        mvc.perform(get("/api/pedidos/sol_101")).andExpect(status().isOk());
    }

    @Test
    void crear_valido_devuelve201() throws Exception {
        when(servicio.crear(any())).thenReturn(pedido("sol_9", EstadoPedido.PENDIENTE));
        mvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("sol_9"));
    }

    @Test
    void crear_sinItems_devuelve400() throws Exception {
        String sinItems = JSON_VALIDO.replaceAll("\\[\\{.*\\}\\]", "[]");
        mvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON).content(sinItems))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.items").exists());
    }

    @Test
    void actualizarDestino_devuelve200() throws Exception {
        when(servicio.actualizarDestino(eq("sol_9"), any())).thenReturn(pedido("sol_9", EstadoPedido.PENDIENTE));
        mvc.perform(put("/api/pedidos/sol_9").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"destino\":\"Antofagasta\",\"modalidad\":\"Express\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void cambiarEstado_devuelve200() throws Exception {
        when(servicio.cambiarEstado("sol_9", EstadoPedido.CONFIRMADO)).thenReturn(pedido("sol_9", EstadoPedido.CONFIRMADO));
        mvc.perform(patch("/api/pedidos/sol_9/estado").contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"CONFIRMADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"));
    }

    @Test
    void cambiarEstado_invalido_devuelve409() throws Exception {
        when(servicio.cambiarEstado("sol_9", EstadoPedido.ENTREGADO)).thenThrow(new ConflictoException("No se puede"));
        mvc.perform(patch("/api/pedidos/sol_9/estado").contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"ENTREGADO\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void eliminar_devuelve204() throws Exception {
        mvc.perform(delete("/api/pedidos/sol_9")).andExpect(status().isNoContent());
    }

    @Test
    void eliminar_enviado_devuelve409() throws Exception {
        doThrow(new ConflictoException("No se puede cancelar")).when(servicio).eliminar("sol_8");
        mvc.perform(delete("/api/pedidos/sol_8")).andExpect(status().isConflict());
    }
}
