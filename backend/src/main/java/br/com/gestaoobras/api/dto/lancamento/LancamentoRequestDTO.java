package br.com.gestaoobras.api.dto.lancamento;

import br.com.gestaoobras.api.domain.enums.StatusPagamento;
import br.com.gestaoobras.api.domain.enums.TipoLancamento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LancamentoRequestDTO(
        @Schema(example = "1")
        @NotNull(message = "O identificador da obra é obrigatório.")
        Long obraId,

        @Schema(example = "1")
        Long centroCustoId,

        @Schema(example = "Compra de 50 sacos de cimento CP-II")
        @NotBlank(message = "A descrição do lançamento é obrigatória.")
        @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres.")
        String descricao,

        @Schema(example = "DESPESA")
        @NotNull(message = "O tipo do lançamento (RECEITA ou DESPESA) é obrigatório.")
        TipoLancamento tipo,

        @Schema(example = "2450.50")
        @NotNull(message = "O valor do lançamento é obrigatório.")
        @DecimalMin(value = "0.01", message = "O valor do lançamento deve ser maior que zero.")
        @Digits(integer = 13, fraction = 2, message = "O valor deve ter no máximo 13 dígitos inteiros e 2 decimais.")
        BigDecimal valor,

        @Schema(example = "2026-03-10")
        @NotNull(message = "A data de competência é obrigatória.")
        LocalDate dataCompetencia,

        @Schema(example = "2026-03-12")
        LocalDate dataPagamento,

        @Schema(example = "PAGO")
        StatusPagamento statusPagamento,

        @Schema(example = "https://storage.exemplo.com/comprovantes/nf-1042.pdf")
        @Size(max = 500, message = "A URL do comprovante deve ter no máximo 500 caracteres.")
        String comprovanteUrl,

        @Schema(example = "Nota fiscal 1042 - Fornecedor ConstruMais")
        String observacoes
) {}
