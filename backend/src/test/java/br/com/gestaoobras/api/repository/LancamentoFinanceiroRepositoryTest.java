package br.com.gestaoobras.api.repository;

import br.com.gestaoobras.api.domain.entity.CentroCusto;
import br.com.gestaoobras.api.domain.entity.LancamentoFinanceiro;
import br.com.gestaoobras.api.domain.entity.Obra;
import br.com.gestaoobras.api.domain.enums.StatusObra;
import br.com.gestaoobras.api.domain.enums.StatusPagamento;
import br.com.gestaoobras.api.domain.enums.TipoLancamento;
import br.com.gestaoobras.api.dto.dashboard.GastoPorCentroCustoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class LancamentoFinanceiroRepositoryTest {

    @Autowired
    private LancamentoFinanceiroRepository lancamentoRepository;

    @Autowired
    private ObraRepository obraRepository;

    @Autowired
    private CentroCustoRepository centroCustoRepository;

    @BeforeEach
    void clean() {
        lancamentoRepository.deleteAll();
        obraRepository.deleteAll();
    }

    @Test
    void deveRetornarZerosEListasVaziasQuandoNaoHouverDados() {
        BigDecimal somaObra = lancamentoRepository.sumByObraAndTipoAndStatus(
                999L, TipoLancamento.DESPESA, StatusPagamento.PAGO);
        BigDecimal somaGlobal = lancamentoRepository.sumGlobalByTipoAndStatus(
                TipoLancamento.RECEITA, StatusPagamento.PAGO);
        List<GastoPorCentroCustoDTO> gastosObra = lancamentoRepository.findDespesasPorCentroCustoByObra(999L);
        List<GastoPorCentroCustoDTO> gastosGlobal = lancamentoRepository.findDespesasPorCentroCustoGlobal();
        List<Object[]> despesasAtivas = lancamentoRepository.sumDespesasAtivasAgrupadasPorObra();
        List<Object[]> despesasPagas = lancamentoRepository.sumDespesasPagasAgrupadasPorObra();

        assertThat(somaObra).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(somaGlobal).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(gastosObra).isEmpty();
        assertThat(gastosGlobal).isEmpty();
        assertThat(despesasAtivas).isEmpty();
        assertThat(despesasPagas).isEmpty();
    }

    @Test
    void deveCalcularQueriesAgregadasDesconsiderandoCancelados() {
        Obra obra = obraRepository.save(Obra.builder()
                .nome("Obra JPA Test")
                .endereco("Rua JPA, 10")
                .responsavel("Eng. Teste")
                .orcamentoPrevisto(new BigDecimal("100000.00"))
                .dataInicio(LocalDate.of(2026, 1, 1))
                .previsaoFim(LocalDate.of(2026, 12, 31))
                .status(StatusObra.EM_ANDAMENTO)
                .progressoPercentual(20)
                .build());

        CentroCusto centro = centroCustoRepository.findAllByAtivoTrueOrderByNomeAsc().getFirst();

        lancamentoRepository.saveAll(List.of(
                LancamentoFinanceiro.builder()
                        .obra(obra).centroCusto(centro).descricao("Despesa Paga")
                        .tipo(TipoLancamento.DESPESA).valor(new BigDecimal("3000.00"))
                        .dataCompetencia(LocalDate.of(2026, 2, 1))
                        .dataPagamento(LocalDate.of(2026, 2, 2))
                        .statusPagamento(StatusPagamento.PAGO).build(),
                LancamentoFinanceiro.builder()
                        .obra(obra).centroCusto(centro).descricao("Despesa Pendente")
                        .tipo(TipoLancamento.DESPESA).valor(new BigDecimal("1500.00"))
                        .dataCompetencia(LocalDate.of(2026, 2, 5))
                        .statusPagamento(StatusPagamento.PENDENTE).build(),
                LancamentoFinanceiro.builder()
                        .obra(obra).centroCusto(centro).descricao("Despesa Cancelada")
                        .tipo(TipoLancamento.DESPESA).valor(new BigDecimal("9999.00"))
                        .dataCompetencia(LocalDate.of(2026, 2, 6))
                        .statusPagamento(StatusPagamento.CANCELADO).build()
        ));

        BigDecimal pagas = lancamentoRepository.sumByObraAndTipoAndStatus(
                obra.getId(), TipoLancamento.DESPESA, StatusPagamento.PAGO);
        BigDecimal pendentes = lancamentoRepository.sumByObraAndTipoAndStatus(
                obra.getId(), TipoLancamento.DESPESA, StatusPagamento.PENDENTE);
        List<GastoPorCentroCustoDTO> porCentro = lancamentoRepository.findDespesasPorCentroCustoByObra(obra.getId());
        List<Object[]> ativasPorObra = lancamentoRepository.sumDespesasAtivasAgrupadasPorObra();
        List<Object[]> pagasPorObra = lancamentoRepository.sumDespesasPagasAgrupadasPorObra();

        assertThat(pagas).isEqualByComparingTo("3000.00");
        assertThat(pendentes).isEqualByComparingTo("1500.00");
        assertThat(porCentro).hasSize(1);
        assertThat(porCentro.getFirst().totalValor()).isEqualByComparingTo("4500.00");
        assertThat(porCentro.getFirst().quantidadeLancamentos()).isEqualTo(2L);
        assertThat(ativasPorObra).hasSize(1);
        assertThat((BigDecimal) ativasPorObra.getFirst()[1]).isEqualByComparingTo("4500.00");
        assertThat(pagasPorObra).hasSize(1);
        assertThat((BigDecimal) pagasPorObra.getFirst()[1]).isEqualByComparingTo("3000.00");
    }
}
