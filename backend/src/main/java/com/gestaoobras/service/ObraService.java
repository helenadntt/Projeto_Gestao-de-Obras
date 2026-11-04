package com.gestaoobras.service;

import com.gestaoobras.dto.ObraRequestDTO;
import com.gestaoobras.dto.ObraResponseDTO;
import com.gestaoobras.model.Obra;
import com.gestaoobras.repository.ObraRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ObraService {

    private final ObraRepository obraRepository;

    public List<ObraResponseDTO> listarTodas() {
        return obraRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public ObraResponseDTO buscarPorId(Long id) {
        Obra obra = obraRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Obra não encontrada com id: " + id));
        return toDTO(obra);
    }

    public ObraResponseDTO criar(ObraRequestDTO dto) {
        Obra obra = Obra.builder()
                .nome(dto.nome())
                .descricao(dto.descricao())
                .endereco(dto.endereco())
                .status(dto.status())
                .dataInicio(dto.dataInicio())
                .dataFim(dto.dataFim())
                .build();
        return toDTO(obraRepository.save(obra));
    }

    public ObraResponseDTO atualizar(Long id, ObraRequestDTO dto) {
        Obra obra = obraRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Obra não encontrada com id: " + id));
        obra.setNome(dto.nome());
        obra.setDescricao(dto.descricao());
        obra.setEndereco(dto.endereco());
        obra.setStatus(dto.status());
        obra.setDataInicio(dto.dataInicio());
        obra.setDataFim(dto.dataFim());
        return toDTO(obraRepository.save(obra));
    }

    public void deletar(Long id) {
        if (!obraRepository.existsById(id)) {
            throw new EntityNotFoundException("Obra não encontrada com id: " + id);
        }
        obraRepository.deleteById(id);
    }

    private ObraResponseDTO toDTO(Obra obra) {
        return new ObraResponseDTO(
                obra.getId(),
                obra.getNome(),
                obra.getDescricao(),
                obra.getEndereco(),
                obra.getStatus(),
                obra.getDataInicio(),
                obra.getDataFim(),
                obra.getCriadoEm(),
                obra.getAtualizadoEm()
        );
    }
}
