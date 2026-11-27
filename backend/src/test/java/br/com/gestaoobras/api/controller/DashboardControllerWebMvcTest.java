package br.com.gestaoobras.api.controller;

import br.com.gestaoobras.api.domain.enums.StatusObra;
import br.com.gestaoobras.api.dto.dashboard.DashboardResumoResponseDTO;
import br.com.gestaoobras.api.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
class DashboardControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    void deveRetornarResumoDoDashboard() throws Exception {
        DashboardResumoResponseDTO resumo = new DashboardResumoResponseDTO(
                2L,
                Map.of(StatusObra.EM_ANDAMENTO, 2L),
                new BigDecimal("45.00"),
                new BigDecimal("200000.00"),
                new BigDecimal("100000.00"),
                BigDecimal.ZERO,
                new BigDecimal("50000.00"),
                BigDecimal.ZERO,
                new BigDecimal("50000.00"),
                new BigDecimal("50000.00"),
                List.of(),
                List.of()
        );

        when(dashboardService.obterResumoGeral()).thenReturn(resumo);

        mockMvc.perform(get("/dashboard/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalObras", is(2)))
                .andExpect(jsonPath("$.saldoRealizadoGeral", is(50000.00)));
    }
}
