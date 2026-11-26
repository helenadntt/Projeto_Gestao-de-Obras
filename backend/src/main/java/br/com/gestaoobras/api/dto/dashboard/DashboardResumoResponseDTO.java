package br.com.gestaoobras.api.dto.dashboard;

import br.com.gestaoobras.api.domain.enums.StatusObra;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardResumoResponseDTO(
        long totalObras,
        Map<StatusObra, Long> obrasPorStatus,
        BigDecimal progressoMedioObras,
        BigDecimal orcamentoTotalPrevisto,
        BigDecimal totalReceitasPagas,
        BigDecimal totalReceitasPendentes,
        BigDecimal totalDespesasPagas,
        BigDecimal totalDespesasPendentes,
        BigDecimal saldoRealizadoGeral,
        BigDecimal saldoProjetadoGeral,
        List<GastoPorCentroCustoDTO> despesasPorCentroCusto,
        List<AlertaObraDTO> alertasObras
) {}
