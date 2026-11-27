package br.com.gestaoobras.api.controller;

import br.com.gestaoobras.api.domain.enums.StatusPagamento;
import br.com.gestaoobras.api.domain.enums.TipoLancamento;
import br.com.gestaoobras.api.dto.lancamento.LancamentoPagamentoUpdateDTO;
import br.com.gestaoobras.api.dto.lancamento.LancamentoRequestDTO;
import br.com.gestaoobras.api.service.LancamentoFinanceiroService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LancamentoFinanceiroController.class)
class LancamentoFinanceiroControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LancamentoFinanceiroService lancamentoService;

    @Test
    void deveRetornar400QuandoCriarLancamentoPendenteComDataPagamento() throws Exception {
        LancamentoRequestDTO invalido = new LancamentoRequestDTO(
                1L,
                1L,
                "Cimento",
                TipoLancamento.DESPESA,
                new BigDecimal("500.00"),
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 2),
                StatusPagamento.PENDENTE,
                null,
                null
        );

        mockMvc.perform(post("/lancamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.fieldErrors[0].field", is("dataPagamento")));
    }

    @Test
    void deveRetornar400QuandoPatchPagamentoPendenteComDataPagamento() throws Exception {
        LancamentoPagamentoUpdateDTO invalido = new LancamentoPagamentoUpdateDTO(
                StatusPagamento.PENDENTE,
                LocalDate.of(2026, 5, 2)
        );

        mockMvc.perform(patch("/lancamentos/1/pagamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.fieldErrors[0].field", is("dataPagamento")));
    }
}
