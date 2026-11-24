package com.museo.productos.service;

import com.museo.productos.dto.DisponibilidadResponse;
import com.museo.productos.dto.ItemStockRequest;
import com.museo.productos.dto.MotivoProblema;
import com.museo.productos.dto.ProductoRequest;
import com.museo.productos.exception.ConflictoException;
import com.museo.productos.exception.DatoInvalidoException;
import com.museo.productos.exception.RecursoNoEncontradoException;
import com.museo.productos.model.Categoria;
import com.museo.productos.model.Producto;
import com.museo.productos.repository.ProductoRepository;
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

/** Prueba las reglas del catálogo sin base de datos: el repositorio JPA es un mock de Mockito. */
@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository repositorio;

    @InjectMocks
    private ProductoService servicio;

    private static Producto producto(String id, Categoria categoria, int stock, boolean activo) {
        return new Producto(id, "Producto " + id, categoria, 10000, stock,
                categoria == Categoria.CURSO ? null : 0.5, "Descripción", activo);
    }

    private static ProductoRequest solicitud(String id, Categoria categoria, Double peso) {
        return new ProductoRequest(id, "Taller de Archivos", categoria, 35000, 12, peso, "  Taller práctico  ");
    }

    @Test
    void listar_sinFiltros_devuelveTodo() {
        when(repositorio.findAllByOrderByIdAsc()).thenReturn(List.of(producto("c01", Categoria.CURSO, 5, true)));
        assertThat(servicio.listar(null, null)).hasSize(1);
    }

    @Test
    void listar_usaElFiltroQueCorresponde() {
        when(repositorio.findByActivoOrderByIdAsc(true)).thenReturn(List.of());
        when(repositorio.findByCategoriaOrderByIdAsc(Categoria.CURSO)).thenReturn(List.of());
        when(repositorio.findByCategoriaAndActivoOrderByIdAsc(Categoria.CURSO, true)).thenReturn(List.of());

        servicio.listar(null, true);
        servicio.listar(Categoria.CURSO, null);
        servicio.listar(Categoria.CURSO, true);

        verify(repositorio).findByActivoOrderByIdAsc(true);
        verify(repositorio).findByCategoriaOrderByIdAsc(Categoria.CURSO);
        verify(repositorio).findByCategoriaAndActivoOrderByIdAsc(Categoria.CURSO, true);
    }

    @Test
    void obtener_inexistente_lanza404() {
        when(repositorio.findById("x99")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> servicio.obtener("x99")).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void crear_curso_asignaIdSinPesoYQuedaActivo() {
        when(repositorio.findAll()).thenReturn(List.of(producto("c01", Categoria.CURSO, 5, true), producto("c04", Categoria.CURSO, 5, true)));
        when(repositorio.existsById("c05")).thenReturn(false);
        when(repositorio.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        Producto creado = servicio.crear(solicitud(null, Categoria.CURSO, 2.0));

        assertThat(creado.getId()).isEqualTo("c05");
        assertThat(creado.getPesoKg()).isNull();          // un curso nunca guarda peso
        assertThat(creado.isActivo()).isTrue();
        assertThat(creado.getDescripcion()).isEqualTo("Taller práctico");
    }

    @Test
    void crear_material_usaPrefijoYSufijo() {
        when(repositorio.findAll()).thenReturn(List.of(producto("m01k", Categoria.MATERIAL_CONSERVACION, 5, true),
                producto("m02k", Categoria.MATERIAL_CONSERVACION, 5, true), producto("c09", Categoria.CURSO, 5, true)));
        assertThat(servicio.siguienteId(Categoria.MATERIAL_CONSERVACION)).isEqualTo("m03k");
        assertThat(servicio.siguienteId(Categoria.PUBLICACION)).isEqualTo("p01");
    }

    @Test
    void crear_productoFisicoSinPeso_lanza400YNoGuarda() {
        assertThatThrownBy(() -> servicio.crear(solicitud(null, Categoria.PUBLICACION, null)))
                .isInstanceOf(DatoInvalidoException.class)
                .extracting("campo").isEqualTo("pesoKg");
        verify(repositorio, never()).save(any());
    }

    @Test
    void crear_conIdRepetido_lanzaConflicto() {
        when(repositorio.existsById("p01")).thenReturn(true);
        assertThatThrownBy(() -> servicio.crear(solicitud("p01", Categoria.PUBLICACION, 0.5)))
                .isInstanceOf(ConflictoException.class);
        verify(repositorio, never()).save(any());
    }

    @Test
    void actualizar_conservaSiEstaActivo() {
        when(repositorio.findById("p01")).thenReturn(Optional.of(producto("p01", Categoria.PUBLICACION, 5, false)));
        when(repositorio.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        Producto editado = servicio.actualizar("p01", solicitud("p01", Categoria.PUBLICACION, 0.7));

        assertThat(editado.getNombre()).isEqualTo("Taller de Archivos");
        assertThat(editado.getPesoKg()).isEqualTo(0.7);
        assertThat(editado.isActivo()).isFalse();
    }

    @Test
    void cambiarActivo_desactivaElProducto() {
        when(repositorio.findById("a01")).thenReturn(Optional.of(producto("a01", Categoria.ARTICULO_MUSEOLOGICO, 5, true)));
        when(repositorio.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));
        assertThat(servicio.cambiarActivo("a01", false).isActivo()).isFalse();
    }

    @Test
    void eliminar_existente_borra_yInexistente_lanza404() {
        when(repositorio.existsById("a01")).thenReturn(true);
        servicio.eliminar("a01");
        verify(repositorio).deleteById("a01");

        when(repositorio.existsById("x99")).thenReturn(false);
        assertThatThrownBy(() -> servicio.eliminar("x99")).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void disponibilidad_detectaSinStockInactivoYNoExiste() {
        when(repositorio.findById("p01")).thenReturn(Optional.of(producto("p01", Categoria.PUBLICACION, 2, true)));
        when(repositorio.findById("a01")).thenReturn(Optional.of(producto("a01", Categoria.ARTICULO_MUSEOLOGICO, 9, false)));
        when(repositorio.findById("x99")).thenReturn(Optional.empty());

        DisponibilidadResponse r = servicio.verificarDisponibilidad(List.of(
                new ItemStockRequest("p01", 3), new ItemStockRequest("a01", 1), new ItemStockRequest("x99", 1)));

        assertThat(r.disponible()).isFalse();
        assertThat(r.problemas()).extracting("motivo")
                .containsExactly(MotivoProblema.SIN_STOCK, MotivoProblema.INACTIVO, MotivoProblema.NO_EXISTE);
        assertThat(r.problemas().get(0).disponible()).isEqualTo(2);
    }

    @Test
    void disponibilidad_sumaLineasRepetidasDelMismoProducto() {
        when(repositorio.findById("p01")).thenReturn(Optional.of(producto("p01", Categoria.PUBLICACION, 3, true)));
        DisponibilidadResponse r = servicio.verificarDisponibilidad(List.of(
                new ItemStockRequest("p01", 2), new ItemStockRequest("p01", 2)));
        assertThat(r.disponible()).isFalse();
        assertThat(r.problemas().get(0).solicitado()).isEqualTo(4);
    }

    @Test
    void descontarStock_conStock_restaYGuarda() {
        when(repositorio.findById("p01")).thenReturn(Optional.of(producto("p01", Categoria.PUBLICACION, 5, true)));
        when(repositorio.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        List<Producto> actualizados = servicio.descontarStock(List.of(new ItemStockRequest("p01", 2)));

        assertThat(actualizados.get(0).getStock()).isEqualTo(3);
    }

    @Test
    void descontarStock_sinStock_lanza409YNoGuardaNada() {
        when(repositorio.findById("p01")).thenReturn(Optional.of(producto("p01", Categoria.PUBLICACION, 1, true)));
        assertThatThrownBy(() -> servicio.descontarStock(List.of(new ItemStockRequest("p01", 2))))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("quedan 1");
        verify(repositorio, never()).save(any());
    }
}
