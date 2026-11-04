package com.gestaoobras.dto;

import com.gestaoobras.model.TipoFinanceiro;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FinanceiroResponseDTO(
        Long id,
        Long obraId,
        TipoFinanceiro tipo,
        String descricao,
        BigDecimal valor,
        LocalDate data,
        LocalDateTime criadoEm
) {}
