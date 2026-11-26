package br.com.gestaoobras.api.dto.dashboard;

import br.com.gestaoobras.api.domain.enums.StatusObra;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AlertaObraDTO(
        Long obraId,
        String obraNome,
        StatusObra status,
        boolean atrasada,
        boolean orcamentoExcedido,
        BigDecimal percentualOrcamentoUtilizado,
        LocalDate previsaoFim
) {}
