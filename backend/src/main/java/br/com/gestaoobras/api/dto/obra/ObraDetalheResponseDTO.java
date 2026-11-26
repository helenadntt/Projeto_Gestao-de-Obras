package br.com.gestaoobras.api.dto.obra;

public record ObraDetalheResponseDTO(
        ObraResponseDTO obra,
        Long diasRestantes,
        ResumoFinanceiroObraDTO resumoFinanceiro
) {}
