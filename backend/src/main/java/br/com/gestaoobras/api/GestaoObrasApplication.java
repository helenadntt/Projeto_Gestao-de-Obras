package br.com.gestaoobras.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

/**
 * Ponto de entrada da API RESTful de Gestão de Obras.
 */
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class GestaoObrasApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestaoObrasApplication.class, args);
    }
}
