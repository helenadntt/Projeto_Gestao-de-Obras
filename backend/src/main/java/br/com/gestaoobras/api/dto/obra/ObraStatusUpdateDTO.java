package br.com.gestaoobras.api.dto.obra;

import br.com.gestaoobras.api.domain.enums.StatusObra;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ObraStatusUpdateDTO(
        @Schema(example = "EM_ANDAMENTO")
        @NotNull(message = "O status da obra é obrigatório.")
        StatusObra status,

        @Schema(example = "60")
        @Min(value = 0, message = "O progresso percentual deve ser no mínimo 0.")
        @Max(value = 100, message = "O progresso percentual deve ser no máximo 100.")
        Integer progressoPercentual,

        @Schema(example = "2026-11-30")
        LocalDate dataConclusao
) {}
