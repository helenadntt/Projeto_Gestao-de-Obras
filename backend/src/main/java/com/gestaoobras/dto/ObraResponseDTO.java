package com.gestaoobras.dto;

import com.gestaoobras.model.StatusObra;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ObraResponseDTO(
        Long id,
        String nome,
        String descricao,
        String endereco,
        StatusObra status,
        LocalDate dataInicio,
        LocalDate dataFim,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {}
