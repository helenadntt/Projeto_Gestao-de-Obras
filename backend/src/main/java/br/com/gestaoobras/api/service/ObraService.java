package br.com.gestaoobras.api.service;

import br.com.gestaoobras.api.domain.entity.Obra;
import br.com.gestaoobras.api.domain.enums.StatusObra;
import br.com.gestaoobras.api.domain.enums.StatusPagamento;
import br.com.gestaoobras.api.domain.enums.TipoLancamento;
import br.com.gestaoobras.api.dto.dashboard.GastoPorCentroCustoDTO;
import br.com.gestaoobras.api.dto.obra.ObraDetalheResponseDTO;
import br.com.gestaoobras.api.dto.obra.ObraRequestDTO;
import br.com.gestaoobras.api.dto.obra.ObraResponseDTO;
import br.com.gestaoobras.api.dto.obra.ObraStatusUpdateDTO;
import br.com.gestaoobras.api.dto.obra.ResumoFinanceiroObraDTO;
import br.com.gestaoobras.api.exception.BusinessRuleException;
import br.com.gestaoobras.api.exception.ResourceNotFoundException;
import br.com.gestaoobras.api.mapper.ObraMapper;
import br.com.gestaoobras.api.repository.LancamentoFinanceiroRepository;
import br.com.gestaoobras.api.repository.ObraRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ObraService {

    private final ObraRepository obraRepository;
    private final LancamentoFinanceiroRepository lancamentoRepository;
    private final ObraMapper obraMapper;

    @Transactional(readOnly = true)
    public Page<ObraResponseDTO> listar(StatusObra status, String busca, Pageable pageable) {
        Specification<Obra> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (busca != null && !busca.isBlank()) {
                String pattern = "%" + busca.trim().toLowerCase() + "%";
                Predicate byNome = cb.like(cb.lower(root.get("nome")), pattern);
                Predicate byCliente = cb.like(cb.lower(cb.coalesce(root.get("cliente"), "")), pattern);
                Predicate byResponsavel = cb.like(cb.lower(root.get("responsavel")), pattern);
                predicates.add(cb.or(byNome, byCliente, byResponsavel));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return obraRepository.findAll(spec, pageable).map(obraMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ObraDetalheResponseDTO buscarDetalhesPorId(Long id) {
        Obra obra = buscarEntidade(id);
        ObraResponseDTO obraDto = obraMapper.toResponse(obra);
        ResumoFinanceiroObraDTO resumo = calcularResumoFinanceiro(obra);
        Long diasRestantes = calcularDiasRestantes(obra);
        return new ObraDetalheResponseDTO(obraDto, diasRestantes, resumo);
    }

    @Transactional(readOnly = true)
    public ResumoFinanceiroObraDTO buscarResumoFinanceiro(Long id) {
        Obra obra = buscarEntidade(id);
        return calcularResumoFinanceiro(obra);
    }

    @Transactional
    public ObraResponseDTO criar(ObraRequestDTO dto) {
        validarDatasObra(dto.dataInicio(), dto.previsaoFim(), dto.dataConclusao());
        Obra obra = obraMapper.toEntity(dto);
        aplicarRegrasDeConclusao(obra);
        return obraMapper.toResponse(obraRepository.save(obra));
    }

    @Transactional
    public ObraResponseDTO atualizar(Long id, ObraRequestDTO dto) {
        Obra obra = buscarEntidade(id);
        validarDatasObra(dto.dataInicio(), dto.previsaoFim(), dto.dataConclusao());
        obraMapper.updateEntity(obra, dto);
        aplicarRegrasDeConclusao(obra);
        return obraMapper.toResponse(obraRepository.save(obra));
    }

    @Transactional
    public ObraResponseDTO atualizarStatus(Long id, ObraStatusUpdateDTO dto) {
        Obra obra = buscarEntidade(id);
        LocalDate dataConclusao = dto.dataConclusao() != null ? dto.dataConclusao() : obra.getDataConclusao();
        validarDatasObra(obra.getDataInicio(), obra.getPrevisaoFim(), dataConclusao);

        obra.setStatus(dto.status());
        if (dto.progressoPercentual() != null) {
            obra.setProgressoPercentual(dto.progressoPercentual());
        }
        if (dto.dataConclusao() != null) {
            obra.setDataConclusao(dto.dataConclusao());
        } else if (dto.status() != StatusObra.CONCLUIDA) {
            obra.setDataConclusao(null);
        }

        aplicarRegrasDeConclusao(obra);
        return obraMapper.toResponse(obraRepository.save(obra));
    }

    @Transactional
    public void excluir(Long id) {
        Obra obra = buscarEntidade(id);
        obraRepository.delete(obra);
    }

    @Transactional(readOnly = true)
    public Obra buscarEntidade(Long id) {
        return obraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Obra não encontrada com ID: " + id));
    }

    private void validarDatasObra(LocalDate dataInicio, LocalDate previsaoFim, LocalDate dataConclusao) {
        if (previsaoFim.isBefore(dataInicio)) {
            throw new BusinessRuleException("A data de previsão de término não pode ser anterior à data de início.");
        }
        if (dataConclusao != null && dataConclusao.isBefore(dataInicio)) {
            throw new BusinessRuleException("A data de conclusão não pode ser anterior à data de início.");
        }
    }

    private void aplicarRegrasDeConclusao(Obra obra) {
        if (obra.getStatus() == StatusObra.CONCLUIDA) {
            if (obra.getDataConclusao() == null) {
                LocalDate hoje = LocalDate.now();
                obra.setDataConclusao(hoje.isBefore(obra.getDataInicio()) ? obra.getDataInicio() : hoje);
            }
            if (obra.getProgressoPercentual() == null || obra.getProgressoPercentual() < 100) {
                obra.setProgressoPercentual(100);
            }
        }
    }

    private Long calcularDiasRestantes(Obra obra) {
        if (obra.getStatus() == StatusObra.CONCLUIDA || obra.getStatus() == StatusObra.CANCELADA) {
            return 0L;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), obra.getPrevisaoFim());
    }

    private ResumoFinanceiroObraDTO calcularResumoFinanceiro(Obra obra) {
        Long obraId = obra.getId();
        BigDecimal receitasPagas = lancamentoRepository.sumByObraAndTipoAndStatus(
                obraId, TipoLancamento.RECEITA, StatusPagamento.PAGO);
        BigDecimal receitasPendentes = lancamentoRepository.sumByObraAndTipoAndStatus(
                obraId, TipoLancamento.RECEITA, StatusPagamento.PENDENTE);
        BigDecimal despesasPagas = lancamentoRepository.sumByObraAndTipoAndStatus(
                obraId, TipoLancamento.DESPESA, StatusPagamento.PAGO);
        BigDecimal despesasPendentes = lancamentoRepository.sumByObraAndTipoAndStatus(
                obraId, TipoLancamento.DESPESA, StatusPagamento.PENDENTE);

        BigDecimal saldoRealizado = receitasPagas.subtract(despesasPagas);
        BigDecimal saldoProjetado = receitasPagas.add(receitasPendentes)
                .subtract(despesasPagas.add(despesasPendentes));

        BigDecimal totalDespesasComprometidas = despesasPagas.add(despesasPendentes);
        BigDecimal orcamentoPrevisto = obra.getOrcamentoPrevisto();
        BigDecimal orcamentoDisponivel = orcamentoPrevisto.subtract(totalDespesasComprometidas);

        BigDecimal percentualUtilizado = BigDecimal.ZERO;
        if (orcamentoPrevisto.compareTo(BigDecimal.ZERO) > 0) {
            percentualUtilizado = totalDespesasComprometidas
                    .multiply(BigDecimal.valueOf(100))
                    .divide(orcamentoPrevisto, 2, RoundingMode.HALF_UP);
        }

        boolean orcamentoExcedido = totalDespesasComprometidas.compareTo(orcamentoPrevisto) > 0;
        long quantidadeLancamentos = lancamentoRepository.countByObraIdAndStatusPagamentoNot(
                obraId, StatusPagamento.CANCELADO);
        List<GastoPorCentroCustoDTO> despesasPorCentroCusto =
                lancamentoRepository.findDespesasPorCentroCustoByObra(obraId);

        return new ResumoFinanceiroObraDTO(
                obraId,
                obra.getNome(),
                orcamentoPrevisto,
                receitasPagas,
                receitasPendentes,
                despesasPagas,
                despesasPendentes,
                saldoRealizado,
                saldoProjetado,
                orcamentoDisponivel,
                percentualUtilizado,
                orcamentoExcedido,
                quantidadeLancamentos,
                despesasPorCentroCusto
        );
    }
}
