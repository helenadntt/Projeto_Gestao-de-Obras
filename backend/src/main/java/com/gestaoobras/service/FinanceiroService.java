package com.gestaoobras.service;

import com.gestaoobras.dto.FinanceiroRequestDTO;
import com.gestaoobras.dto.FinanceiroResponseDTO;
import com.gestaoobras.model.Financeiro;
import com.gestaoobras.model.Obra;
import com.gestaoobras.repository.FinanceiroRepository;
import com.gestaoobras.repository.ObraRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FinanceiroService {

    private final FinanceiroRepository financeiroRepository;
    private final ObraRepository obraRepository;

    public List<FinanceiroResponseDTO> listarPorObra(Long obraId) {
        return financeiroRepository.findByObraId(obraId).stream()
                .map(this::toDTO)
                .toList();
    }

    public FinanceiroResponseDTO criar(Long obraId, FinanceiroRequestDTO dto) {
        Obra obra = obraRepository.findById(obraId)
                .orElseThrow(() -> new EntityNotFoundException("Obra não encontrada com id: " + obraId));

        Financeiro financeiro = Financeiro.builder()
                .obra(obra)
                .tipo(dto.tipo())
                .descricao(dto.descricao())
                .valor(dto.valor())
                .data(dto.data())
                .build();

        return toDTO(financeiroRepository.save(financeiro));
    }

    public void deletar(Long id) {
        if (!financeiroRepository.existsById(id)) {
            throw new EntityNotFoundException("Lançamento financeiro não encontrado com id: " + id);
        }
        financeiroRepository.deleteById(id);
    }

    private FinanceiroResponseDTO toDTO(Financeiro f) {
        return new FinanceiroResponseDTO(
                f.getId(),
                f.getObra().getId(),
                f.getTipo(),
                f.getDescricao(),
                f.getValor(),
                f.getData(),
                f.getCriadoEm()
        );
    }
}
