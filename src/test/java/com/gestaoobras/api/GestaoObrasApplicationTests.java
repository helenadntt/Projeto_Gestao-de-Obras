package com.gestaoobras.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=none"
})
class GestaoObrasApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que o contexto Spring sobe sem erros
    }
}
