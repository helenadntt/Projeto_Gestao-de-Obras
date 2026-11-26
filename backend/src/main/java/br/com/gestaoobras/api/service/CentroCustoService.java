package br.com.gestaoobras.api.service;

import br.com.gestaoobras.api.domain.entity.CentroCusto;
import br.com.gestaoobras.api.dto.centrocusto.CentroCustoRequestDTO;
import br.com.gestaoobras.api.dto.centrocusto.CentroCustoResponseDTO;
import br.com.gestaoobras.api.exception.ConflictException;
import br.com.gestaoobras.api.exception.ResourceNotFoundException;
import br.com.gestaoobras.api.mapper.CentroCustoMapper;
import br.com.gestaoobras.api.repository.CentroCustoRepository;
import br.com.gestaoobras.api.repository.LancamentoFinanceiroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CentroCustoService {

    private final CentroCustoRepository centroCustoRepository;
    private final LancamentoFinanceiroRepository lancamentoRepository;
    private final CentroCustoMapper mapper;

    @Transactional(readOnly = true)
    public List<CentroCustoResponseDTO> listar(boolean apenasAtivos) {
        List<CentroCusto> lista = apenasAtivos
                ? centroCustoRepository.findAllByAtivoTrueOrderByNomeAsc()
                : centroCustoRepository.findAllByOrderByNomeAsc();
        return lista.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CentroCustoResponseDTO buscarPorId(Long id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    public CentroCustoResponseDTO criar(CentroCustoRequestDTO dto) {
        String nome = dto.nome().trim();
        if (centroCustoRepository.existsByNomeIgnoreCase(nome)) {
            throw new ConflictException("Já existe um centro de custo cadastrado com o nome '" + nome + "'.");
        }
        CentroCusto salvo = centroCustoRepository.save(mapper.toEntity(dto));
        return mapper.toResponse(salvo);
    }

    @Transactional
    public CentroCustoResponseDTO atualizar(Long id, CentroCustoRequestDTO dto) {
        CentroCusto entity = buscarEntidade(id);
        String nome = dto.nome().trim();
        if (centroCustoRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ConflictException("Já existe outro centro de custo cadastrado com o nome '" + nome + "'.");
        }
        mapper.updateEntity(entity, dto);
        return mapper.toResponse(centroCustoRepository.save(entity));
    }

    @Transactional
    public void excluir(Long id) {
        CentroCusto entity = buscarEntidade(id);
        lancamentoRepository.desvincularCentroCusto(id);
        centroCustoRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public CentroCusto buscarEntidade(Long id) {
        return centroCustoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Centro de custo não encontrado com ID: " + id));
    }
}
