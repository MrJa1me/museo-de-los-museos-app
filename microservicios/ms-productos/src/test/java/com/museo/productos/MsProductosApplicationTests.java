package com.museo.productos;

import com.museo.productos.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/** Levanta el microservicio completo con una H2 en memoria y verifica que se cargan los datos semilla. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:pruebas;DB_CLOSE_DELAY=-1")
class MsProductosApplicationTests {

    @Autowired
    private ProductoRepository repositorio;

    @Test
    void contextoCargaYSiembraLosDiezProductos() {
        assertThat(repositorio.count()).isEqualTo(10);
        assertThat(repositorio.findById("a02")).get().extracting(p -> p.getStock()).isEqualTo(0);
    }
}
