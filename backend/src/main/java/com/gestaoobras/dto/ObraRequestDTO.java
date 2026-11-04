package com.gestaoobras.dto;

import com.gestaoobras.model.StatusObra;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ObraRequestDTO(
        @NotBlank(message = "O nome da obra é obrigatório.")
        String nome,

        String descricao,

        String endereco,

        @NotNull(message = "O status é obrigatório.")
        StatusObra status,

        @NotNull(message = "A data de início é obrigatória.")
        LocalDate dataInicio,

        LocalDate dataFim
) {}
