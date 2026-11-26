package br.com.gestaoobras.api.dto.obra;

import br.com.gestaoobras.api.domain.enums.StatusObra;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ObraResponseDTO(
        Long id,
        String nome,
        String descricao,
        String cliente,
        String endereco,
        String responsavel,
        BigDecimal orcamentoPrevisto,
        LocalDate dataInicio,
        LocalDate previsaoFim,
        LocalDate dataConclusao,
        StatusObra status,
        Integer progressoPercentual,
        boolean atrasada,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
