package br.com.gestaoobras.api.dto.dashboard;

import br.com.gestaoobras.api.domain.enums.SeveridadeAlerta;
import br.com.gestaoobras.api.domain.enums.StatusObra;
import br.com.gestaoobras.api.domain.enums.TipoAlerta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AlertaObraDTO(
        Long obraId,
        String obraNome,
        StatusObra status,
        boolean atrasada,
        boolean orcamentoExcedido,
        BigDecimal percentualOrcamentoUtilizado,
        LocalDate previsaoFim,
        boolean prazoProximo,
        boolean orcamentoEmRisco,
        boolean avancoDefasado,
        SeveridadeAlerta severidade,
        List<TipoAlerta> tipos
) {}
