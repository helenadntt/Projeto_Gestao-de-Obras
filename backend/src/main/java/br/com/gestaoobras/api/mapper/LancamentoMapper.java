package br.com.gestaoobras.api.mapper;

import br.com.gestaoobras.api.domain.entity.CentroCusto;
import br.com.gestaoobras.api.domain.entity.LancamentoFinanceiro;
import br.com.gestaoobras.api.domain.entity.Obra;
import br.com.gestaoobras.api.domain.enums.StatusPagamento;
import br.com.gestaoobras.api.dto.lancamento.LancamentoRequestDTO;
import br.com.gestaoobras.api.dto.lancamento.LancamentoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class LancamentoMapper {

    public LancamentoFinanceiro toEntity(LancamentoRequestDTO dto, Obra obra, CentroCusto centroCusto) {
        return LancamentoFinanceiro.builder()
                .obra(obra)
                .centroCusto(centroCusto)
                .descricao(dto.descricao().trim())
                .tipo(dto.tipo())
                .valor(dto.valor())
                .dataCompetencia(dto.dataCompetencia())
                .dataPagamento(dto.dataPagamento())
                .statusPagamento(dto.statusPagamento() != null ? dto.statusPagamento() : StatusPagamento.PENDENTE)
                .comprovanteUrl(trimOrNull(dto.comprovanteUrl()))
                .observacoes(trimOrNull(dto.observacoes()))
                .build();
    }

    public void updateEntity(LancamentoFinanceiro entity, LancamentoRequestDTO dto, Obra obra, CentroCusto centroCusto) {
        entity.setObra(obra);
        entity.setCentroCusto(centroCusto);
        entity.setDescricao(dto.descricao().trim());
        entity.setTipo(dto.tipo());
        entity.setValor(dto.valor());
        entity.setDataCompetencia(dto.dataCompetencia());
        entity.setDataPagamento(dto.dataPagamento());
        if (dto.statusPagamento() != null) {
            entity.setStatusPagamento(dto.statusPagamento());
        }
        entity.setComprovanteUrl(trimOrNull(dto.comprovanteUrl()));
        entity.setObservacoes(trimOrNull(dto.observacoes()));
    }

    public LancamentoResponseDTO toResponse(LancamentoFinanceiro entity) {
        CentroCusto cc = entity.getCentroCusto();
        return new LancamentoResponseDTO(
                entity.getId(),
                entity.getObra().getId(),
                entity.getObra().getNome(),
                cc != null ? cc.getId() : null,
                cc != null ? cc.getNome() : null,
                entity.getDescricao(),
                entity.getTipo(),
                entity.getValor(),
                entity.getDataCompetencia(),
                entity.getDataPagamento(),
                entity.getStatusPagamento(),
                entity.getComprovanteUrl(),
                entity.getObservacoes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
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
