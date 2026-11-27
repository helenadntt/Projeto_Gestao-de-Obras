package br.com.gestaoobras.api.service;

import br.com.gestaoobras.api.domain.entity.CentroCusto;
import br.com.gestaoobras.api.domain.entity.LancamentoFinanceiro;
import br.com.gestaoobras.api.domain.entity.Obra;
import br.com.gestaoobras.api.domain.enums.StatusObra;
import br.com.gestaoobras.api.domain.enums.StatusPagamento;
import br.com.gestaoobras.api.domain.enums.TipoLancamento;
import br.com.gestaoobras.api.dto.lancamento.LancamentoPagamentoUpdateDTO;
import br.com.gestaoobras.api.dto.lancamento.LancamentoRequestDTO;
import br.com.gestaoobras.api.dto.lancamento.LancamentoResponseDTO;
import br.com.gestaoobras.api.exception.BusinessRuleException;
import br.com.gestaoobras.api.exception.ConflictException;
import br.com.gestaoobras.api.exception.ResourceNotFoundException;
import br.com.gestaoobras.api.mapper.LancamentoMapper;
import br.com.gestaoobras.api.repository.LancamentoFinanceiroRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LancamentoFinanceiroService {

    private final LancamentoFinanceiroRepository lancamentoRepository;
    private final ObraService obraService;
    private final CentroCustoService centroCustoService;
    private final LancamentoMapper mapper;

    @Transactional(readOnly = true)
    public Page<LancamentoResponseDTO> listar(
            Long obraId,
            Long centroCustoId,
            TipoLancamento tipo,
            StatusPagamento statusPagamento,
            LocalDate dataInicio,
            LocalDate dataFim,
            Pageable pageable
    ) {
        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new BusinessRuleException("A data final do filtro não pode ser anterior à data inicial.");
        }

        Specification<LancamentoFinanceiro> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (obraId != null) {
                predicates.add(cb.equal(root.get("obra").get("id"), obraId));
            }
            if (centroCustoId != null) {
                predicates.add(cb.equal(root.get("centroCusto").get("id"), centroCustoId));
            }
            if (tipo != null) {
                predicates.add(cb.equal(root.get("tipo"), tipo));
            }
            if (statusPagamento != null) {
                predicates.add(cb.equal(root.get("statusPagamento"), statusPagamento));
            }
            if (dataInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataCompetencia"), dataInicio));
            }
            if (dataFim != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dataCompetencia"), dataFim));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return lancamentoRepository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<LancamentoResponseDTO> listarPorObra(
            Long obraId,
            TipoLancamento tipo,
            StatusPagamento statusPagamento,
            Pageable pageable
    ) {
        obraService.buscarEntidade(obraId);
        return listar(obraId, null, tipo, statusPagamento, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public LancamentoResponseDTO buscarPorId(Long id) {
        return mapper.toResponse(buscarEntidadeComDetalhes(id));
    }

    @Transactional
    public LancamentoResponseDTO criar(LancamentoRequestDTO dto) {
        Obra obra = obraService.buscarEntidade(dto.obraId());
        validarObraAceitaLancamentos(obra);
        CentroCusto centroCusto = resolverCentroCusto(dto.centroCustoId(), true);

        LancamentoFinanceiro entity = mapper.toEntity(dto, obra, centroCusto);
        ajustarDataEStatusPagamento(entity);

        LancamentoFinanceiro salvo = lancamentoRepository.save(entity);
        return mapper.toResponse(salvo);
    }

    @Transactional
    public LancamentoResponseDTO atualizar(Long id, LancamentoRequestDTO dto) {
        LancamentoFinanceiro entity = buscarEntidadeComDetalhes(id);
        validarObraAceitaLancamentos(entity.getObra());

        Obra obra = obraService.buscarEntidade(dto.obraId());
        validarObraAceitaLancamentos(obra);

        CentroCusto centroCusto = resolverCentroCusto(dto.centroCustoId(), true);

        mapper.updateEntity(entity, dto, obra, centroCusto);
        ajustarDataEStatusPagamento(entity);

        return mapper.toResponse(lancamentoRepository.save(entity));
    }

    @Transactional
    public LancamentoResponseDTO atualizarPagamento(Long id, LancamentoPagamentoUpdateDTO dto) {
        LancamentoFinanceiro entity = buscarEntidadeComDetalhes(id);
        entity.setStatusPagamento(dto.statusPagamento());
        entity.setDataPagamento(dto.dataPagamento());
        ajustarDataEStatusPagamento(entity);
        return mapper.toResponse(lancamentoRepository.save(entity));
    }

    @Transactional
    public void excluir(Long id) {
        LancamentoFinanceiro entity = buscarEntidadeComDetalhes(id);
        lancamentoRepository.delete(entity);
    }

    private void validarObraAceitaLancamentos(Obra obra) {
        if (obra.getStatus() == StatusObra.CANCELADA) {
            throw new ConflictException(
                    "Não é possível registrar ou editar lançamentos financeiros em uma obra cancelada."
            );
        }
    }

    private CentroCusto resolverCentroCusto(Long centroCustoId, boolean exigirAtivo) {
        if (centroCustoId == null) {
            return null;
        }
        CentroCusto centroCusto = centroCustoService.buscarEntidade(centroCustoId);
        if (exigirAtivo && Boolean.FALSE.equals(centroCusto.getAtivo())) {
            throw new ConflictException(
                    "Não é possível vincular um lançamento ao centro de custo inativo: " + centroCusto.getNome()
            );
        }
        return centroCusto;
    }

    private void ajustarDataEStatusPagamento(LancamentoFinanceiro entity) {
        if (entity.getStatusPagamento() == StatusPagamento.PAGO && entity.getDataPagamento() == null) {
            entity.setDataPagamento(LocalDate.now());
        } else if (entity.getStatusPagamento() == StatusPagamento.PENDENTE
                || entity.getStatusPagamento() == StatusPagamento.CANCELADO) {
            entity.setDataPagamento(null);
        }
    }

    private LancamentoFinanceiro buscarEntidadeComDetalhes(Long id) {
        return lancamentoRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lançamento financeiro não encontrado com ID: " + id));
    }
}
