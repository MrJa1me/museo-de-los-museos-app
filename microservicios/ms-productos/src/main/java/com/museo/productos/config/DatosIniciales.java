package com.museo.productos.config;

import com.museo.productos.model.Categoria;
import com.museo.productos.model.Producto;
import com.museo.productos.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Carga los 10 productos ficticios del equipo (los mismos de SeedData.kt en la app)
 * solo la primera vez, cuando la tabla está vacía. La taza (a02) queda agotada a propósito.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final ProductoRepository repositorio;

    public DatosIniciales(ProductoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void run(String... args) {
        if (repositorio.count() > 0) return;
        repositorio.saveAll(productosSemilla());
        log.info("Se cargaron {} productos de prueba", repositorio.count());
    }

    static List<Producto> productosSemilla() {
        return List.of(
                new Producto("c01", "Introducción a la Museología", Categoria.CURSO, 45000, 30, null,
                        "Curso introductorio a los conceptos y prácticas de la museología.", true),
                new Producto("c02", "Mediación Cultural en Museos", Categoria.CURSO, 52000, 25, null,
                        "Estrategias para acercar las colecciones a distintos públicos.", true),
                new Producto("c03", "Gestión de Audiencias", Categoria.CURSO, 58000, 20, null,
                        "Herramientas para conocer, atraer y fidelizar audiencias.", true),
                new Producto("c04", "Conservación Preventiva", Categoria.CURSO, 60000, 15, null,
                        "Buenas prácticas para cuidar colecciones y prevenir su deterioro.", true),
                new Producto("p01", "Guía de Museos Virtuales (libro)", Categoria.PUBLICACION, 12000, 25, 0.5,
                        "Guía impresa con una selección de museos virtuales de Iberoamérica.", true),
                new Producto("p02", "Cuaderno de Mediación (libro)", Categoria.PUBLICACION, 9000, 40, 0.4,
                        "Cuaderno de actividades para planificar experiencias de mediación.", true),
                new Producto("m01k", "Kit de guantes y papel libre de ácido", Categoria.MATERIAL_CONSERVACION, 18000, 30, 0.8,
                        "Kit básico para manipular y proteger documentos y objetos.", true),
                new Producto("m02k", "Caja de conservación archivística", Categoria.MATERIAL_CONSERVACION, 24000, 15, 1.5,
                        "Caja libre de ácido para guardar documentos de forma segura.", true),
                new Producto("a01", "Bolso de tela Museo de los Museos", Categoria.ARTICULO_MUSEOLOGICO, 8000, 50, 0.2,
                        "Bolso de tela reutilizable con el logo del museo.", true),
                new Producto("a02", "Taza Museo de los Museos", Categoria.ARTICULO_MUSEOLOGICO, 7000, 0, 0.4,
                        "Taza de cerámica con el logo del museo. Agotada.", true)
        );
    }
}
