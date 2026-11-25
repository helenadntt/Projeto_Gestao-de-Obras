package br.com.gestaoobras.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal da aplicação Gestão de Obras.
 *
 * <p>Ponto de entrada do Spring Boot. O Flyway é executado automaticamente
 * antes que qualquer bean de JPA seja inicializado, garantindo que o schema
 * esteja atualizado antes do {@code ddl-auto=validate} ser aplicado.
 */
@SpringBootApplication
public class GestaoObrasApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestaoObrasApplication.class, args);
    }
}
