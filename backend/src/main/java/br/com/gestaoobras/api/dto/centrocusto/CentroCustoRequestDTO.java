package br.com.gestaoobras.api.dto.centrocusto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CentroCustoRequestDTO(
        @Schema(example = "Instalações Elétricas")
        @NotBlank(message = "O nome do centro de custo é obrigatório.")
        @Size(max = 100, message = "O nome do centro de custo deve ter no máximo 100 caracteres.")
        String nome,

        @Schema(example = "Fiação, quadros de distribuição e serviços elétricos")
        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres.")
        String descricao,

        @Schema(example = "true")
        Boolean ativo
) {}
