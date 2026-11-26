package br.com.gestaoobras.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: verifica que o contexto Spring sobe corretamente.
 *
 * O perfil "test" desabilita Flyway e JPA validation para que o teste
 * rode sem banco de dados, garantindo que a CI não dependa de infraestrutura.
 */
@SpringBootTest
@ActiveProfiles("test")
class GestaoObrasApplicationTests {

    @Test
    void contextLoads() {
        // Nenhuma asserção necessária:
        // se o contexto subir sem exceção, o teste passa.
    }
}
