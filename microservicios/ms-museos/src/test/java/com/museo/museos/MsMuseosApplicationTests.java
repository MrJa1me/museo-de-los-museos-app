package com.museo.museos;

import com.museo.museos.repository.MuseoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/** Levanta el microservicio completo con una H2 en memoria y verifica que se cargan los datos semilla. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:pruebas;DB_CLOSE_DELAY=-1")
class MsMuseosApplicationTests {

    @Autowired
    private MuseoRepository repositorio;

    @Test
    void contextoCargaYSiembraLosDiezMuseos() {
        assertThat(repositorio.count()).isEqualTo(10);
    }
}
