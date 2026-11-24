package com.museo.pedidos;

import com.museo.pedidos.repository.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/** Levanta el microservicio completo con una H2 en memoria y verifica los datos semilla con sus ítems. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:pruebas;DB_CLOSE_DELAY=-1")
class MsPedidosApplicationTests {

    @Autowired
    private PedidoRepository repositorio;

    @Test
    void contextoCargaYSiembraLosPedidos() {
        assertThat(repositorio.count()).isEqualTo(2);
        assertThat(repositorio.findById("sol_101")).get().satisfies(p -> {
            assertThat(p.getItems()).hasSize(1);
            assertThat(p.getTotal()).isEqualTo(15500);
        });
    }
}
