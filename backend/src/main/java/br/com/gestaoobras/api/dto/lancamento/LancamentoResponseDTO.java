package br.com.gestaoobras.api.dto.lancamento;

import br.com.gestaoobras.api.domain.enums.StatusPagamento;
import br.com.gestaoobras.api.domain.enums.TipoLancamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record LancamentoResponseDTO(
        Long id,
        Long obraId,
        String obraNome,
        Long centroCustoId,
        String centroCustoNome,
        String descricao,
        TipoLancamento tipo,
        BigDecimal valor,
        LocalDate dataCompetencia,
        LocalDate dataPagamento,
        StatusPagamento statusPagamento,
        String comprovanteUrl,
        String observacoes,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
