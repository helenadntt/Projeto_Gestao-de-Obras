package br.com.gestaoobras.api.dto.dashboard;

import java.math.BigDecimal;

public record GastoPorCentroCustoDTO(
        Long centroCustoId,
        String centroCustoNome,
        BigDecimal totalValor,
        long quantidadeLancamentos
) {}
