package com.gestaoobras.dto;

import com.gestaoobras.model.TipoFinanceiro;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinanceiroRequestDTO(
        @NotNull(message = "O tipo financeiro é obrigatório.")
        TipoFinanceiro tipo,

        @NotBlank(message = "A descrição é obrigatória.")
        String descricao,

        @NotNull(message = "O valor é obrigatório.")
        @Positive(message = "O valor deve ser positivo.")
        BigDecimal valor,

        @NotNull(message = "A data é obrigatória.")
        LocalDate data
) {}
