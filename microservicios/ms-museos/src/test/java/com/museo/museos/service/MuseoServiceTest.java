package com.museo.museos.service;

import com.museo.museos.dto.MuseoRequest;
import com.museo.museos.exception.ConflictoException;
import com.museo.museos.exception.RecursoNoEncontradoException;
import com.museo.museos.model.Clasificacion;
import com.museo.museos.model.Museo;
import com.museo.museos.repository.MuseoRepository;
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

/** Prueba la lógica del servicio sin base de datos: el repositorio es un mock de Mockito. */
@ExtendWith(MockitoExtension.class)
class MuseoServiceTest {

    @Mock
    private MuseoRepository repositorio;

    @InjectMocks
    private MuseoService servicio;

    private static MuseoRequest solicitud(String id) {
        return new MuseoRequest(id, "Museo Virtual de Prueba", "Chile", "Valparaíso", -33.04, -71.61,
                Clasificacion.ARTE, "Descripción de prueba con más de veinte caracteres.",
                "https://ejemplo.museo.test/m11", null, null);
    }

    private static Museo museo(String id, boolean publicado) {
        return new Museo(id, "Museo " + id, "Chile", "Santiago", -33.4, -70.6, Clasificacion.HISTORIA,
                "Descripción", "https://ejemplo.museo.test/" + id, "", publicado);
    }

    @Test
    void listar_sinFiltro_devuelveTodas() {
        when(repositorio.findAllByOrderByNombreAsc()).thenReturn(List.of(museo("m01", true), museo("m02", false)));
        assertThat(servicio.listar(null)).hasSize(2);
    }

    @Test
    void listar_soloPublicadas() {
        when(repositorio.findByPublicadoOrderByNombreAsc(true)).thenReturn(List.of(museo("m01", true)));
        assertThat(servicio.listar(true)).extracting(Museo::getId).containsExactly("m01");
    }

    @Test
    void obtener_inexistente_lanza404() {
        when(repositorio.findById("m99")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> servicio.obtener("m99")).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void crear_sinId_asignaElSiguienteYQuedaPendiente() {
        when(repositorio.findAll()).thenReturn(List.of(museo("m01", true), museo("m10", true)));
        when(repositorio.existsById("m11")).thenReturn(false);
        when(repositorio.save(any(Museo.class))).thenAnswer(i -> i.getArgument(0));

        Museo creado = servicio.crear(solicitud(null));

        assertThat(creado.getId()).isEqualTo("m11");
        assertThat(creado.isPublicado()).isFalse();
        assertThat(creado.getProyecto()).isEqualTo("Mapa Iberoamérica 2026");
    }

    @Test
    void crear_conIdRepetido_lanzaConflicto() {
        when(repositorio.existsById("m01")).thenReturn(true);
        assertThatThrownBy(() -> servicio.crear(solicitud("m01"))).isInstanceOf(ConflictoException.class);
        verify(repositorio, never()).save(any());
    }

    @Test
    void actualizar_conservaElEstadoDePublicacion() {
        Museo existente = museo("m01", true);
        when(repositorio.findById("m01")).thenReturn(Optional.of(existente));
        when(repositorio.save(any(Museo.class))).thenAnswer(i -> i.getArgument(0));

        Museo actualizado = servicio.actualizar("m01", solicitud("m01"));

        assertThat(actualizado.getNombre()).isEqualTo("Museo Virtual de Prueba");
        assertThat(actualizado.isPublicado()).isTrue();
    }

    @Test
    void aprobar_publicaLaFicha() {
        when(repositorio.findById("m11")).thenReturn(Optional.of(museo("m11", false)));
        when(repositorio.save(any(Museo.class))).thenAnswer(i -> i.getArgument(0));
        assertThat(servicio.aprobar("m11").isPublicado()).isTrue();
    }

    @Test
    void eliminar_existente_borra() {
        when(repositorio.existsById("m01")).thenReturn(true);
        servicio.eliminar("m01");
        verify(repositorio).deleteById("m01");
    }

    @Test
    void eliminar_inexistente_lanza404() {
        when(repositorio.existsById("m99")).thenReturn(false);
        assertThatThrownBy(() -> servicio.eliminar("m99")).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void siguienteId_ignoraIdsConOtroFormato() {
        when(repositorio.findAll()).thenReturn(List.of(museo("m03", true), museo("xyz", true)));
        assertThat(servicio.siguienteId()).isEqualTo("m04");
    }
}
