package br.com.gestaoobras.api.mapper;

import br.com.gestaoobras.api.domain.entity.CentroCusto;
import br.com.gestaoobras.api.dto.centrocusto.CentroCustoRequestDTO;
import br.com.gestaoobras.api.dto.centrocusto.CentroCustoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class CentroCustoMapper {

    public CentroCusto toEntity(CentroCustoRequestDTO dto) {
        return CentroCusto.builder()
                .nome(dto.nome().trim())
                .descricao(trimOrNull(dto.descricao()))
                .ativo(dto.ativo() != null ? dto.ativo() : Boolean.TRUE)
                .build();
    }

    public void updateEntity(CentroCusto entity, CentroCustoRequestDTO dto) {
        entity.setNome(dto.nome().trim());
        entity.setDescricao(trimOrNull(dto.descricao()));
        if (dto.ativo() != null) {
            entity.setAtivo(dto.ativo());
        }
    }

    public CentroCustoResponseDTO toResponse(CentroCusto entity) {
        return new CentroCustoResponseDTO(
                entity.getId(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getAtivo(),
                entity.getCreatedAt()
        );
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
