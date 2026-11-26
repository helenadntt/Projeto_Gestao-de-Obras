package br.com.gestaoobras.api.controller;

import br.com.gestaoobras.api.dto.centrocusto.CentroCustoRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CentroCustoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveListarCentrosCustoPadraoECriarAtualizarExcluirNovo() throws Exception {
        mockMvc.perform(get("/centros-custo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(6))));

        CentroCustoRequestDTO novo = new CentroCustoRequestDTO(
                "Paisagismo",
                "Jardins e áreas externas",
                true
        );

        String response = mockMvc.perform(post("/centros-custo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Paisagismo")))
                .andExpect(jsonPath("$.ativo", is(true)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        // Duplicidade deve retornar 409 Conflict
        mockMvc.perform(post("/centros-custo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CentroCustoRequestDTO("paisagismo", "Duplicado", true))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));

        // Atualizar
        mockMvc.perform(put("/centros-custo/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CentroCustoRequestDTO("Paisagismo e Urbanismo", "Atualizado", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Paisagismo e Urbanismo")))
                .andExpect(jsonPath("$.ativo", is(false)));

        // Excluir
        mockMvc.perform(delete("/centros-custo/{id}", id))
                .andExpect(status().isNoContent());
    }
}
