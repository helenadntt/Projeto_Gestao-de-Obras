package br.com.gestaoobras.api;

import br.com.gestaoobras.api.repository.CentroCustoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GestaoObrasApplicationTests {

    @Autowired
    private CentroCustoRepository centroCustoRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoadsAndFlywaySeedExecuted() {
        assertThat(centroCustoRepository.count()).isGreaterThanOrEqualTo(6);
    }

    @Test
    void deveExporDocumentacaoOpenApiEHealthCheck() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title", is("Gestão de Obras API")));

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }
}
