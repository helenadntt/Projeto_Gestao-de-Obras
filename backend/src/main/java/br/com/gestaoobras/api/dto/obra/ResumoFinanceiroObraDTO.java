package br.com.gestaoobras.api.dto.obra;

import br.com.gestaoobras.api.dto.dashboard.GastoPorCentroCustoDTO;

import java.math.BigDecimal;
import java.util.List;

public record ResumoFinanceiroObraDTO(
        Long obraId,
        String nomeObra,
        BigDecimal orcamentoPrevisto,
        BigDecimal totalReceitasPagas,
        BigDecimal totalReceitasPendentes,
        BigDecimal totalDespesasPagas,
        BigDecimal totalDespesasPendentes,
        BigDecimal saldoRealizado,
        BigDecimal saldoProjetado,
        BigDecimal orcamentoDisponivel,
        BigDecimal percentualOrcamentoUtilizado,
        boolean orcamentoExcedido,
        long quantidadeLancamentos,
        List<GastoPorCentroCustoDTO> despesasPorCentroCusto
) {}
