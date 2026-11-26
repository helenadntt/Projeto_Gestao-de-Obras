package br.com.gestaoobras.api.dto.obra;

import br.com.gestaoobras.api.domain.enums.StatusObra;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ObraRequestDTO(
        @Schema(example = "Residencial Parque das Flores")
        @NotBlank(message = "O nome da obra é obrigatório.")
        @Size(max = 150, message = "O nome da obra deve ter no máximo 150 caracteres.")
        String nome,

        @Schema(example = "Construção de edifício residencial com 12 apartamentos")
        String descricao,

        @Schema(example = "Construtora Horizonte Ltda")
        @Size(max = 150, message = "O nome do cliente deve ter no máximo 150 caracteres.")
        String cliente,

        @Schema(example = "Av. Paulista, 1200 - São Paulo/SP")
        @NotBlank(message = "O endereço da obra é obrigatório.")
        @Size(max = 255, message = "O endereço deve ter no máximo 255 caracteres.")
        String endereco,

        @Schema(example = "Eng. Carlos Silva")
        @NotBlank(message = "O responsável técnico é obrigatório.")
        @Size(max = 100, message = "O nome do responsável deve ter no máximo 100 caracteres.")
        String responsavel,

        @Schema(example = "450000.00")
        @NotNull(message = "O orçamento previsto é obrigatório.")
        @DecimalMin(value = "0.00", message = "O orçamento previsto não pode ser negativo.")
        @Digits(integer = 13, fraction = 2, message = "O orçamento previsto deve ter no máximo 13 dígitos inteiros e 2 decimais.")
        BigDecimal orcamentoPrevisto,

        @Schema(example = "2026-01-15")
        @NotNull(message = "A data de início é obrigatória.")
        LocalDate dataInicio,

        @Schema(example = "2026-12-20")
        @NotNull(message = "A data de previsão de término é obrigatória.")
        LocalDate previsaoFim,

        @Schema(example = "2026-12-10")
        LocalDate dataConclusao,

        @Schema(example = "PLANEJAMENTO")
        StatusObra status,

        @Schema(example = "25")
        @Min(value = 0, message = "O progresso percentual deve ser no mínimo 0.")
        @Max(value = 100, message = "O progresso percentual deve ser no máximo 100.")
        Integer progressoPercentual
) {}
