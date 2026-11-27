package br.com.gestaoobras.api.controller;

import br.com.gestaoobras.api.dto.centrocusto.CentroCustoRequestDTO;
import br.com.gestaoobras.api.dto.centrocusto.CentroCustoResponseDTO;
import br.com.gestaoobras.api.exception.ResourceNotFoundException;
import br.com.gestaoobras.api.service.CentroCustoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CentroCustoController.class)
class CentroCustoControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CentroCustoService centroCustoService;

    @Test
    void deveListarCentrosCusto() throws Exception {
        when(centroCustoService.listar(true)).thenReturn(List.of(
                new CentroCustoResponseDTO(1L, "Material", "Insumos", true, null)
        ));

        mockMvc.perform(get("/centros-custo").param("apenasAtivos", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Material")));
    }

    @Test
    void deveCriarCentroCustoRetornandoLocation() throws Exception {
        when(centroCustoService.criar(any(CentroCustoRequestDTO.class)))
                .thenReturn(new CentroCustoResponseDTO(10L, "Paisagismo", "Jardins", true, null));

        mockMvc.perform(post("/centros-custo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CentroCustoRequestDTO("Paisagismo", "Jardins", true))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.nome", is("Paisagismo")));
    }

    @Test
    void deveRetornar400QuandoNomeDoCentroCustoForEmBranco() throws Exception {
        mockMvc.perform(post("/centros-custo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CentroCustoRequestDTO("  ", null, true))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.fieldErrors[0].field", is("nome")));
    }

    @Test
    void deveRetornar404QuandoCentroCustoNaoExistir() throws Exception {
        when(centroCustoService.buscarPorId(99L))
                .thenThrow(new ResourceNotFoundException("Centro de custo não encontrado com ID: 99"));

        mockMvc.perform(get("/centros-custo/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Centro de custo não encontrado com ID: 99")));
    }
}
