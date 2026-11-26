package br.com.gestaoobras.api.mapper;

import br.com.gestaoobras.api.domain.entity.Obra;
import br.com.gestaoobras.api.domain.enums.StatusObra;
import br.com.gestaoobras.api.dto.obra.ObraRequestDTO;
import br.com.gestaoobras.api.dto.obra.ObraResponseDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ObraMapper {

    public Obra toEntity(ObraRequestDTO dto) {
        return Obra.builder()
                .nome(dto.nome().trim())
                .descricao(trimOrNull(dto.descricao()))
                .cliente(trimOrNull(dto.cliente()))
                .endereco(dto.endereco().trim())
                .responsavel(dto.responsavel().trim())
                .orcamentoPrevisto(dto.orcamentoPrevisto())
                .dataInicio(dto.dataInicio())
                .previsaoFim(dto.previsaoFim())
                .dataConclusao(dto.dataConclusao())
                .status(dto.status() != null ? dto.status() : StatusObra.PLANEJAMENTO)
                .progressoPercentual(dto.progressoPercentual() != null ? dto.progressoPercentual() : 0)
                .build();
    }

    public void updateEntity(Obra obra, ObraRequestDTO dto) {
        obra.setNome(dto.nome().trim());
        obra.setDescricao(trimOrNull(dto.descricao()));
        obra.setCliente(trimOrNull(dto.cliente()));
        obra.setEndereco(dto.endereco().trim());
        obra.setResponsavel(dto.responsavel().trim());
        obra.setOrcamentoPrevisto(dto.orcamentoPrevisto());
        obra.setDataInicio(dto.dataInicio());
        obra.setPrevisaoFim(dto.previsaoFim());
        obra.setDataConclusao(dto.dataConclusao());
        if (dto.status() != null) {
            obra.setStatus(dto.status());
        }
        if (dto.progressoPercentual() != null) {
            obra.setProgressoPercentual(dto.progressoPercentual());
        }
    }

    public ObraResponseDTO toResponse(Obra obra) {
        return new ObraResponseDTO(
                obra.getId(),
                obra.getNome(),
                obra.getDescricao(),
                obra.getCliente(),
                obra.getEndereco(),
                obra.getResponsavel(),
                obra.getOrcamentoPrevisto(),
                obra.getDataInicio(),
                obra.getPrevisaoFim(),
                obra.getDataConclusao(),
                obra.getStatus(),
                obra.getProgressoPercentual(),
                isAtrasada(obra),
                obra.getCreatedAt(),
                obra.getUpdatedAt()
        );
    }

    public boolean isAtrasada(Obra obra) {
        return obra.getStatus() != StatusObra.CONCLUIDA
                && obra.getStatus() != StatusObra.CANCELADA
                && obra.getPrevisaoFim() != null
                && obra.getPrevisaoFim().isBefore(LocalDate.now());
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
