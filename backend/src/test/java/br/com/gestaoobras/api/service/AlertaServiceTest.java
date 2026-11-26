package br.com.gestaoobras.api.service;

import br.com.gestaoobras.api.domain.entity.Obra;
import br.com.gestaoobras.api.domain.enums.SeveridadeAlerta;
import br.com.gestaoobras.api.domain.enums.StatusObra;
import br.com.gestaoobras.api.domain.enums.TipoAlerta;
import br.com.gestaoobras.api.dto.dashboard.AlertaObraDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AlertaServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 6, 15);
    private static final Clock CLOCK_FIXO = Clock.fixed(
            Instant.parse("2026-06-15T12:00:00Z"),
            ZoneOffset.UTC
    );

    private AlertaService alertaService;

    @BeforeEach
    void setUp() {
        alertaService = new AlertaService(
                CLOCK_FIXO,
                7L,
                new BigDecimal("80"),
                new BigDecimal("20")
        );
    }

    @Test
    void deveGerarAlertaCriticoQuandoPrazoVencido() {
        Obra obra = criarObra(1L, "Obra Atrasada", StatusObra.EM_ANDAMENTO,
                HOJE.minusDays(1), new BigDecimal("100000.00"), 90);

        List<AlertaObraDTO> alertas = alertaService.gerarAlertas(
                List.of(obra),
                Map.of(1L, new BigDecimal("50000.00")),
                Map.of(1L, new BigDecimal("50000.00"))
        );

        assertThat(alertas).hasSize(1);
        AlertaObraDTO alerta = alertas.getFirst();
        assertThat(alerta.atrasada()).isTrue();
        assertThat(alerta.prazoProximo()).isFalse();
        assertThat(alerta.severidade()).isEqualTo(SeveridadeAlerta.CRITICO);
        assertThat(alerta.tipos()).containsExactly(TipoAlerta.PRAZO_VENCIDO);
    }

    @Test
    void deveGerarAlertaCriticoQuandoOrcamentoEstourado() {
        Obra obra = criarObra(2L, "Obra Estourada", StatusObra.EM_ANDAMENTO,
                HOJE.plusDays(30), new BigDecimal("100000.00"), 95);

        List<AlertaObraDTO> alertas = alertaService.gerarAlertas(
                List.of(obra),
                Map.of(2L, new BigDecimal("105000.00")),
                Map.of(2L, new BigDecimal("105000.00"))
        );

        assertThat(alertas).hasSize(1);
        AlertaObraDTO alerta = alertas.getFirst();
        assertThat(alerta.orcamentoExcedido()).isTrue();
        assertThat(alerta.orcamentoEmRisco()).isFalse();
        assertThat(alerta.severidade()).isEqualTo(SeveridadeAlerta.CRITICO);
        assertThat(alerta.tipos()).containsExactly(TipoAlerta.ORCAMENTO_ESTOURADO);
    }

    @Test
    void deveGerarAlertaAtencaoQuandoPrazoProximo() {
        Obra obra = criarObra(3L, "Obra Prazo Proximo", StatusObra.EM_ANDAMENTO,
                HOJE.plusDays(7), new BigDecimal("100000.00"), 80);

        List<AlertaObraDTO> alertas = alertaService.gerarAlertas(
                List.of(obra),
                Map.of(3L, new BigDecimal("70000.00")),
                Map.of(3L, new BigDecimal("70000.00"))
        );

        assertThat(alertas).hasSize(1);
        AlertaObraDTO alerta = alertas.getFirst();
        assertThat(alerta.prazoProximo()).isTrue();
        assertThat(alerta.atrasada()).isFalse();
        assertThat(alerta.severidade()).isEqualTo(SeveridadeAlerta.ATENCAO);
        assertThat(alerta.tipos()).containsExactly(TipoAlerta.PRAZO_PROXIMO);
    }

    @Test
    void deveGerarAlertaAtencaoQuandoOrcamentoEmRiscoSemEstourar() {
        Obra obra = criarObra(4L, "Obra Em Risco", StatusObra.EM_ANDAMENTO,
                HOJE.plusDays(30), new BigDecimal("100000.00"), 80);

        List<AlertaObraDTO> alertas = alertaService.gerarAlertas(
                List.of(obra),
                Map.of(4L, new BigDecimal("85000.00")),
                Map.of(4L, new BigDecimal("80000.00"))
        );

        assertThat(alertas).hasSize(1);
        AlertaObraDTO alerta = alertas.getFirst();
        assertThat(alerta.orcamentoEmRisco()).isTrue();
        assertThat(alerta.orcamentoExcedido()).isFalse();
        assertThat(alerta.severidade()).isEqualTo(SeveridadeAlerta.ATENCAO);
        assertThat(alerta.tipos()).containsExactly(TipoAlerta.ORCAMENTO_EM_RISCO);
    }

    @Test
    void deveGerarAlertaAtencaoQuandoAvancoDefasado() {
        // 65% do orçamento consumido com apenas 40% de avanço físico -> diferença de 25 pontos (> 20)
        Obra obra = criarObra(5L, "Obra Avanco Defasado", StatusObra.EM_ANDAMENTO,
                HOJE.plusDays(30), new BigDecimal("100000.00"), 40);

        List<AlertaObraDTO> alertas = alertaService.gerarAlertas(
                List.of(obra),
                Map.of(5L, new BigDecimal("65000.00")),
                Map.of(5L, new BigDecimal("50000.00"))
        );

        assertThat(alertas).hasSize(1);
        AlertaObraDTO alerta = alertas.getFirst();
        assertThat(alerta.avancoDefasado()).isTrue();
        assertThat(alerta.severidade()).isEqualTo(SeveridadeAlerta.ATENCAO);
        assertThat(alerta.tipos()).containsExactly(TipoAlerta.AVANCO_DEFASADO);
    }

    @Test
    void deveIgnorarObraCanceladaEOrcamentoNuloOuZero() {
        Obra cancelada = criarObra(6L, "Obra Cancelada", StatusObra.CANCELADA,
                HOJE.minusDays(10), new BigDecimal("10000.00"), 10);
        Obra orcamentoZero = criarObra(7L, "Obra Orcamento Zero", StatusObra.EM_ANDAMENTO,
                HOJE.plusDays(30), BigDecimal.ZERO, 0);
        Obra orcamentoNulo = criarObra(8L, "Obra Orcamento Nulo", StatusObra.EM_ANDAMENTO,
                HOJE.plusDays(30), null, 0);

        List<AlertaObraDTO> alertas = alertaService.gerarAlertas(
                List.of(cancelada, orcamentoZero, orcamentoNulo),
                Map.of(6L, new BigDecimal("50000.00"), 7L, new BigDecimal("5000.00"), 8L, new BigDecimal("5000.00")),
                Map.of(6L, new BigDecimal("50000.00"), 7L, new BigDecimal("5000.00"), 8L, new BigDecimal("5000.00"))
        );

        assertThat(alertas).isEmpty();
    }

    @Test
    void deveIgnorarPrazoEAvancoParaObraConcluidaMasAlertarSeOrcamentoEstourado() {
        Obra concluida = criarObra(9L, "Obra Concluida", StatusObra.CONCLUIDA,
                HOJE.minusDays(5), new BigDecimal("100000.00"), 100);

        List<AlertaObraDTO> semEstouro = alertaService.gerarAlertas(
                List.of(concluida),
                Map.of(9L, new BigDecimal("90000.00")),
                Map.of(9L, new BigDecimal("90000.00"))
        );
        // Orçamento em risco (>= 80% pago) ainda se aplica em obra concluída dentro do orçamento;
        // prazo vencido e avanço defasado não se aplicam.
        assertThat(semEstouro).hasSize(1);
        assertThat(semEstouro.getFirst().atrasada()).isFalse();
        assertThat(semEstouro.getFirst().prazoProximo()).isFalse();
        assertThat(semEstouro.getFirst().avancoDefasado()).isFalse();
        assertThat(semEstouro.getFirst().orcamentoEmRisco()).isTrue();
    }

    @Test
    void deveOrdenarPorSeveridadeCriticoPrimeiroEDepoisPorNomeDaObra() {
        Obra atencaoZ = criarObra(10L, "Zeta Atencao", StatusObra.EM_ANDAMENTO,
                HOJE.plusDays(3), new BigDecimal("100000.00"), 90);
        Obra criticoB = criarObra(11L, "Beta Critico", StatusObra.EM_ANDAMENTO,
                HOJE.minusDays(2), new BigDecimal("100000.00"), 90);
        Obra criticoA = criarObra(12L, "Alfa Critico", StatusObra.EM_ANDAMENTO,
                HOJE.plusDays(20), new BigDecimal("100000.00"), 95);

        List<AlertaObraDTO> alertas = alertaService.gerarAlertas(
                List.of(atencaoZ, criticoB, criticoA),
                Map.of(
                        10L, new BigDecimal("50000.00"),
                        11L, new BigDecimal("50000.00"),
                        12L, new BigDecimal("120000.00")
                ),
                Map.of()
        );

        assertThat(alertas)
                .extracting(AlertaObraDTO::obraNome)
                .containsExactly("Alfa Critico", "Beta Critico", "Zeta Atencao");
    }

    private Obra criarObra(
            Long id,
            String nome,
            StatusObra status,
            LocalDate previsaoFim,
            BigDecimal orcamento,
            Integer progresso
    ) {
        return Obra.builder()
                .id(id)
                .nome(nome)
                .endereco("Rua Teste, 100")
                .responsavel("Eng. Teste")
                .status(status)
                .dataInicio(HOJE.minusDays(30))
                .previsaoFim(previsaoFim)
                .orcamentoPrevisto(orcamento)
                .progressoPercentual(progresso)
                .build();
    }
}
