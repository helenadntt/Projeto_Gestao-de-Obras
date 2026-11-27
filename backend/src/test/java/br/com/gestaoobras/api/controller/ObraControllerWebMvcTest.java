package br.com.gestaoobras.api.controller;

import br.com.gestaoobras.api.config.PageableConfig;
import br.com.gestaoobras.api.dto.obra.ObraRequestDTO;
import br.com.gestaoobras.api.service.LancamentoFinanceiroService;
import br.com.gestaoobras.api.service.ObraService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ObraController.class)
@ImportAutoConfiguration(SpringDataWebAutoConfiguration.class)
@Import(PageableConfig.class)
class ObraControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ObraService obraService;

    @MockitoBean
    private LancamentoFinanceiroService lancamentoService;

    @Test
    void deveLimitarTamanhoMaximoDePaginaEm100() throws Exception {
        when(obraService.listar(any(), any(), any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/obras").param("size", "250"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(obraService).listar(any(), any(), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);
    }

    @Test
    void deveRetornar400QuandoPeriodoDaObraForInvalido() throws Exception {
        ObraRequestDTO invalida = new ObraRequestDTO(
                "Obra Período Inválido",
                null,
                null,
                "Rua A, 10",
                "Eng. João",
                new BigDecimal("10000.00"),
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 1),
                null,
                null,
                0
        );

        mockMvc.perform(post("/obras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.fieldErrors[0].field", is("previsaoFim")));
    }
}
