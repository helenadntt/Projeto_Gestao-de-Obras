package br.com.gestaoobras.api.service;

import br.com.gestaoobras.api.domain.entity.Obra;
import br.com.gestaoobras.api.domain.enums.StatusObra;
import br.com.gestaoobras.api.domain.enums.StatusPagamento;
import br.com.gestaoobras.api.domain.enums.TipoLancamento;
import br.com.gestaoobras.api.dto.dashboard.AlertaObraDTO;
import br.com.gestaoobras.api.dto.dashboard.DashboardResumoResponseDTO;
import br.com.gestaoobras.api.dto.dashboard.GastoPorCentroCustoDTO;
import br.com.gestaoobras.api.repository.LancamentoFinanceiroRepository;
import br.com.gestaoobras.api.repository.ObraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ObraRepository obraRepository;
    private final LancamentoFinanceiroRepository lancamentoRepository;
    private final AlertaService alertaService;

    @Transactional(readOnly = true)
    public DashboardResumoResponseDTO obterResumoGeral() {
        List<Obra> obras = obraRepository.findAll();

        Map<StatusObra, Long> obrasPorStatus = new EnumMap<>(StatusObra.class);
        for (StatusObra status : StatusObra.values()) {
            obrasPorStatus.put(status, 0L);
        }

        BigDecimal orcamentoTotalPrevisto = BigDecimal.ZERO;
        long somaProgresso = 0L;

        for (Obra obra : obras) {
            obrasPorStatus.merge(obra.getStatus(), 1L, Long::sum);
            orcamentoTotalPrevisto = orcamentoTotalPrevisto.add(obra.getOrcamentoPrevisto());
            somaProgresso += obra.getProgressoPercentual() != null ? obra.getProgressoPercentual() : 0;
        }

        BigDecimal progressoMedio = obras.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(somaProgresso)
                .divide(BigDecimal.valueOf(obras.size()), 2, RoundingMode.HALF_UP);

        BigDecimal totalReceitasPagas = lancamentoRepository.sumGlobalByTipoAndStatus(
                TipoLancamento.RECEITA, StatusPagamento.PAGO);
        BigDecimal totalReceitasPendentes = lancamentoRepository.sumGlobalByTipoAndStatus(
                TipoLancamento.RECEITA, StatusPagamento.PENDENTE);
        BigDecimal totalDespesasPagas = lancamentoRepository.sumGlobalByTipoAndStatus(
                TipoLancamento.DESPESA, StatusPagamento.PAGO);
        BigDecimal totalDespesasPendentes = lancamentoRepository.sumGlobalByTipoAndStatus(
                TipoLancamento.DESPESA, StatusPagamento.PENDENTE);

        BigDecimal saldoRealizadoGeral = totalReceitasPagas.subtract(totalDespesasPagas);
        BigDecimal saldoProjetadoGeral = totalReceitasPagas.add(totalReceitasPendentes)
                .subtract(totalDespesasPagas.add(totalDespesasPendentes));

        List<GastoPorCentroCustoDTO> despesasPorCentroCusto =
                lancamentoRepository.findDespesasPorCentroCustoGlobal();

        Map<Long, BigDecimal> despesasPorObra = new HashMap<>();
        for (Object[] row : lancamentoRepository.sumDespesasAtivasAgrupadasPorObra()) {
            despesasPorObra.put((Long) row[0], (BigDecimal) row[1]);
        }

        Map<Long, BigDecimal> despesasPagasPorObra = new HashMap<>();
        for (Object[] row : lancamentoRepository.sumDespesasPagasAgrupadasPorObra()) {
            despesasPagasPorObra.put((Long) row[0], (BigDecimal) row[1]);
        }

        List<AlertaObraDTO> alertas = alertaService.gerarAlertas(obras, despesasPorObra, despesasPagasPorObra);

        return new DashboardResumoResponseDTO(
                obras.size(),
                obrasPorStatus,
                progressoMedio,
                orcamentoTotalPrevisto,
                totalReceitasPagas,
                totalReceitasPendentes,
                totalDespesasPagas,
                totalDespesasPendentes,
                saldoRealizadoGeral,
                saldoProjetadoGeral,
                despesasPorCentroCusto,
                alertas
        );
    }
}
