package com.museo.museos.controller;

import com.museo.museos.exception.RecursoNoEncontradoException;
import com.museo.museos.model.Clasificacion;
import com.museo.museos.model.Museo;
import com.museo.museos.service.MuseoService;
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
@WebMvcTest(MuseoController.class)
class MuseoControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MuseoService servicio;

    private static final String JSON_VALIDO = """
            {"nombre":"Museo Virtual de Prueba","pais":"Chile","ciudad":"Valparaíso",
             "latitud":-33.04,"longitud":-71.61,"clasificacion":"ARTE",
             "descripcion":"Descripción de prueba con más de veinte caracteres.",
             "enlace":"https://ejemplo.museo.test/m11"}
            """;

    private static Museo museo(String id, boolean publicado) {
        return new Museo(id, "Museo " + id, "Chile", "Santiago", -33.4, -70.6, Clasificacion.HISTORIA,
                "Descripción", "https://ejemplo.museo.test/" + id, "", publicado);
    }

    @Test
    void listar_devuelve200ConLaLista() throws Exception {
        when(servicio.listar(true)).thenReturn(List.of(museo("m01", true)));
        mvc.perform(get("/api/museos").param("publicado", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("m01"))
                .andExpect(jsonPath("$[0].publicado").value(true));
    }

    @Test
    void obtener_inexistente_devuelve404() throws Exception {
        when(servicio.obtener("m99")).thenThrow(new RecursoNoEncontradoException("No existe el museo m99"));
        mvc.perform(get("/api/museos/m99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe el museo m99"));
    }

    @Test
    void crear_valido_devuelve201() throws Exception {
        when(servicio.crear(any())).thenReturn(museo("m11", false));
        mvc.perform(post("/api/museos").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("m11"));
    }

    @Test
    void crear_conDatosInvalidos_devuelve400ConElCampo() throws Exception {
        String invalido = JSON_VALIDO.replace("https://ejemplo.museo.test/m11", "ftp://mal");
        mvc.perform(post("/api/museos").contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.enlace").exists());
    }

    @Test
    void crear_conJsonRoto_devuelve400() throws Exception {
        mvc.perform(post("/api/museos").contentType(MediaType.APPLICATION_JSON).content("{roto"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizar_devuelve200() throws Exception {
        when(servicio.actualizar(eq("m01"), any())).thenReturn(museo("m01", true));
        mvc.perform(put("/api/museos/m01").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isOk());
    }

    @Test
    void aprobar_devuelveLaFichaPublicada() throws Exception {
        when(servicio.aprobar("m11")).thenReturn(museo("m11", true));
        mvc.perform(patch("/api/museos/m11/aprobar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicado").value(true));
    }

    @Test
    void eliminar_devuelve204() throws Exception {
        mvc.perform(delete("/api/museos/m01")).andExpect(status().isNoContent());
        verify(servicio).eliminar("m01");
    }

    @Test
    void eliminar_inexistente_devuelve404() throws Exception {
        doThrow(new RecursoNoEncontradoException("No existe")).when(servicio).eliminar("m99");
        mvc.perform(delete("/api/museos/m99")).andExpect(status().isNotFound());
    }
}
