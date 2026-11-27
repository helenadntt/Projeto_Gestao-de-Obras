package br.com.gestaoobras.api.dto.lancamento;

import br.com.gestaoobras.api.domain.enums.StatusPagamento;
import br.com.gestaoobras.api.dto.validation.PagamentoValido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@PagamentoValido
public record LancamentoPagamentoUpdateDTO(
        @Schema(example = "PAGO")
        @NotNull(message = "O status de pagamento é obrigatório.")
        StatusPagamento statusPagamento,

        @Schema(example = "2026-03-15")
        LocalDate dataPagamento
) {}
