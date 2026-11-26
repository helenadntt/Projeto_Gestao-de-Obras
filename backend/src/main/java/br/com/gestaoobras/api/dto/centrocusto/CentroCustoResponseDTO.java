package br.com.gestaoobras.api.dto.centrocusto;

import java.time.OffsetDateTime;

public record CentroCustoResponseDTO(
        Long id,
        String nome,
        String descricao,
        Boolean ativo,
        OffsetDateTime createdAt
) {}
