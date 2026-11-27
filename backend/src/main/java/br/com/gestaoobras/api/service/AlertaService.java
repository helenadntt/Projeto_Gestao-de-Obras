package br.com.gestaoobras.api.service;

import br.com.gestaoobras.api.domain.entity.Obra;
import br.com.gestaoobras.api.domain.enums.SeveridadeAlerta;
import br.com.gestaoobras.api.domain.enums.StatusObra;
import br.com.gestaoobras.api.domain.enums.TipoAlerta;
import br.com.gestaoobras.api.dto.dashboard.AlertaObraDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AlertaService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final Clock clock;
    private final long diasPrazoProximo;
    private final BigDecimal percentualOrcamentoRisco;
    private final BigDecimal diferencaAvanco;

    public AlertaService(
            Clock clock,
            @Value("${app.alertas.dias-prazo-proximo:7}") long diasPrazoProximo,
            @Value("${app.alertas.percentual-orcamento-risco:80}") BigDecimal percentualOrcamentoRisco,
            @Value("${app.alertas.diferenca-avanco:20}") BigDecimal diferencaAvanco
    ) {
        this.clock = clock;
        this.diasPrazoProximo = diasPrazoProximo;
        this.percentualOrcamentoRisco = percentualOrcamentoRisco;
        this.diferencaAvanco = diferencaAvanco;
    }

    public List<AlertaObraDTO> gerarAlertas(
            List<Obra> obras,
            Map<Long, BigDecimal> despesasAtivasPorObra,
            Map<Long, BigDecimal> despesasPagasPorObra
    ) {
        LocalDate hoje = LocalDate.now(clock);

        return obras.stream()
                .filter(obra -> obra.getStatus() != StatusObra.CANCELADA)
                .map(obra -> avaliarObra(obra, hoje, despesasAtivasPorObra, despesasPagasPorObra))
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing(AlertaObraDTO::severidade)
                        .thenComparing(AlertaObraDTO::obraNome, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(AlertaObraDTO::obraId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private AlertaObraDTO avaliarObra(
            Obra obra,
            LocalDate hoje,
            Map<Long, BigDecimal> despesasAtivasPorObra,
            Map<Long, BigDecimal> despesasPagasPorObra
    ) {
        boolean naoConcluida = obra.getStatus() != StatusObra.CONCLUIDA;

        boolean atrasada = naoConcluida
                && obra.getPrevisaoFim() != null
                && obra.getPrevisaoFim().isBefore(hoje);

        boolean prazoProximo = naoConcluida
                && !atrasada
                && obra.getPrevisaoFim() != null
                && ChronoUnit.DAYS.between(hoje, obra.getPrevisaoFim()) <= diasPrazoProximo;

        BigDecimal orcamento = obra.getOrcamentoPrevisto();
        boolean orcamentoValido = orcamento != null && orcamento.compareTo(BigDecimal.ZERO) > 0;

        BigDecimal percentualUtilizado = BigDecimal.ZERO;
        boolean orcamentoExcedido = false;
        boolean orcamentoEmRisco = false;
        boolean avancoDefasado = false;

        if (orcamentoValido) {
            BigDecimal despesasAtivas = despesasAtivasPorObra.getOrDefault(obra.getId(), BigDecimal.ZERO);
            BigDecimal despesasPagas = despesasPagasPorObra.getOrDefault(obra.getId(), BigDecimal.ZERO);

            percentualUtilizado = despesasAtivas
                    .multiply(CEM)
                    .divide(orcamento, 2, RoundingMode.HALF_UP);

            orcamentoExcedido = despesasAtivas.compareTo(orcamento) > 0;

            BigDecimal percentualPago = despesasPagas
                    .multiply(CEM)
                    .divide(orcamento, 2, RoundingMode.HALF_UP);

            orcamentoEmRisco = !orcamentoExcedido
                    && despesasPagas.compareTo(orcamento) <= 0
                    && percentualPago.compareTo(percentualOrcamentoRisco) >= 0;

            int progresso = obra.getProgressoPercentual() != null ? obra.getProgressoPercentual() : 0;
            avancoDefasado = naoConcluida
                    && percentualUtilizado.subtract(BigDecimal.valueOf(progresso)).compareTo(diferencaAvanco) > 0;
        }

        List<TipoAlerta> tipos = new ArrayList<>();
        if (atrasada) {
            tipos.add(TipoAlerta.PRAZO_VENCIDO);
        }
        if (orcamentoExcedido) {
            tipos.add(TipoAlerta.ORCAMENTO_ESTOURADO);
        }
        if (prazoProximo) {
            tipos.add(TipoAlerta.PRAZO_PROXIMO);
        }
        if (orcamentoEmRisco) {
            tipos.add(TipoAlerta.ORCAMENTO_EM_RISCO);
        }
        if (avancoDefasado) {
            tipos.add(TipoAlerta.AVANCO_DEFASADO);
        }

        if (tipos.isEmpty()) {
            return null;
        }

        SeveridadeAlerta severidade = (atrasada || orcamentoExcedido)
                ? SeveridadeAlerta.CRITICO
                : SeveridadeAlerta.ATENCAO;

        return new AlertaObraDTO(
                obra.getId(),
                obra.getNome(),
                obra.getStatus(),
                atrasada,
                orcamentoExcedido,
                percentualUtilizado,
                obra.getPrevisaoFim(),
                prazoProximo,
                orcamentoEmRisco,
                avancoDefasado,
                severidade,
                List.copyOf(tipos)
        );
    }
}
