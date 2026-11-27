package br.com.gestaoobras.api.service;

import br.com.gestaoobras.api.domain.entity.Obra;
import br.com.gestaoobras.api.domain.enums.StatusObra;
import br.com.gestaoobras.api.dto.obra.ObraRequestDTO;
import br.com.gestaoobras.api.dto.obra.ObraResponseDTO;
import br.com.gestaoobras.api.dto.obra.ObraStatusUpdateDTO;
import br.com.gestaoobras.api.dto.obra.ResumoFinanceiroObraDTO;
import br.com.gestaoobras.api.exception.BusinessRuleException;
import br.com.gestaoobras.api.exception.ConflictException;
import br.com.gestaoobras.api.exception.ResourceNotFoundException;
import br.com.gestaoobras.api.mapper.ObraMapper;
import br.com.gestaoobras.api.repository.LancamentoFinanceiroRepository;
import br.com.gestaoobras.api.repository.ObraRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObraServiceTest {

    @Mock
    private ObraRepository obraRepository;

    @Mock
    private LancamentoFinanceiroRepository lancamentoRepository;

    @Spy
    private ObraMapper obraMapper = new ObraMapper();

    @InjectMocks
    private ObraService obraService;

    @Test
    void deveCriarObraConcluidaPreenchendoProgresso100EDataConclusao() {
        ObraRequestDTO dto = new ObraRequestDTO(
                "Obra Teste",
                "Descrição",
                "Cliente X",
                "Rua A, 10",
                "Eng. João",
                new BigDecimal("100000.00"),
                LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(10),
                null,
                StatusObra.CONCLUIDA,
                50
        );

        when(obraRepository.save(any(Obra.class))).thenAnswer(inv -> {
            Obra salva = inv.getArgument(0);
            salva.setId(1L);
            return salva;
        });

        ObraResponseDTO response = obraService.criar(dto);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(StatusObra.CONCLUIDA);
        assertThat(response.progressoPercentual()).isEqualTo(100);
        assertThat(response.dataConclusao()).isNotNull();
    }

    @Test
    void deveLancarResourceNotFoundQuandoObraNaoExiste() {
        when(obraRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> obraService.buscarEntidade(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deveExcluirObraQuandoNaoPossuiLancamentos() {
        Obra obra = Obra.builder().id(1L).nome("Obra Sem Lançamentos").build();
        when(obraRepository.findById(1L)).thenReturn(Optional.of(obra));
        when(lancamentoRepository.existsByObraId(1L)).thenReturn(false);

        obraService.excluir(1L);

        verify(obraRepository).delete(obra);
    }

    @Test
    void deveLancarConflictAoExcluirObraComLancamentos() {
        Obra obra = Obra.builder().id(1L).nome("Obra Com Lançamentos").build();
        when(obraRepository.findById(1L)).thenReturn(Optional.of(obra));
        when(lancamentoRepository.existsByObraId(1L)).thenReturn(true);

        assertThatThrownBy(() -> obraService.excluir(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("lançamentos");

        verify(obraRepository, never()).delete(any(Obra.class));
    }

    @Test
    void deveListarObrasMapeandoResultadoPaginado() {
        Obra obra = Obra.builder()
                .id(1L)
                .nome("Obra Listada")
                .orcamentoPrevisto(new BigDecimal("10000.00"))
                .dataInicio(LocalDate.now())
                .previsaoFim(LocalDate.now().plusDays(30))
                .status(StatusObra.EM_ANDAMENTO)
                .progressoPercentual(10)
                .build();

        when(obraRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(obra)));

        Page<ObraResponseDTO> pagina = obraService.listar(
                StatusObra.EM_ANDAMENTO, "Obra", PageRequest.of(0, 10));

        assertThat(pagina.getTotalElements()).isEqualTo(1);
        assertThat(pagina.getContent().getFirst().nome()).isEqualTo("Obra Listada");
    }

    @Test
    void deveAtualizarObraExistente() {
        Obra existente = Obra.builder()
                .id(1L)
                .nome("Obra Antiga")
                .endereco("Rua Antiga, 1")
                .responsavel("Eng. Antigo")
                .orcamentoPrevisto(new BigDecimal("10000.00"))
                .dataInicio(LocalDate.of(2026, 1, 1))
                .previsaoFim(LocalDate.of(2026, 12, 31))
                .status(StatusObra.EM_ANDAMENTO)
                .progressoPercentual(10)
                .build();

        when(obraRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(obraRepository.save(any(Obra.class))).thenAnswer(inv -> inv.getArgument(0));

        ObraRequestDTO dto = new ObraRequestDTO(
                "Obra Atualizada",
                "Nova descrição",
                "Cliente Y",
                "Rua Nova, 2",
                "Eng. Novo",
                new BigDecimal("20000.00"),
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2027, 2, 1),
                null,
                StatusObra.EM_ANDAMENTO,
                45
        );

        ObraResponseDTO response = obraService.atualizar(1L, dto);

        assertThat(response.nome()).isEqualTo("Obra Atualizada");
        assertThat(response.orcamentoPrevisto()).isEqualByComparingTo("20000.00");
        assertThat(response.progressoPercentual()).isEqualTo(45);
    }

    @Test
    void deveLancarBusinessRuleAoAtualizarComPrevisaoAnteriorAInicio() {
        Obra existente = Obra.builder()
                .id(1L)
                .nome("Obra")
                .orcamentoPrevisto(BigDecimal.ZERO)
                .dataInicio(LocalDate.of(2026, 1, 1))
                .previsaoFim(LocalDate.of(2026, 12, 31))
                .status(StatusObra.EM_ANDAMENTO)
                .progressoPercentual(0)
                .build();

        when(obraRepository.findById(1L)).thenReturn(Optional.of(existente));

        ObraRequestDTO dto = new ObraRequestDTO(
                "Obra",
                null,
                null,
                "Rua A, 1",
                "Eng. A",
                BigDecimal.ZERO,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 5, 1),
                null,
                StatusObra.EM_ANDAMENTO,
                0
        );

        assertThatThrownBy(() -> obraService.atualizar(1L, dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("previsão de término");

        verify(obraRepository, never()).save(any(Obra.class));
    }

    @Test
    void deveAtualizarStatusLimpandoDataConclusaoAoReabrirObra() {
        Obra existente = Obra.builder()
                .id(1L)
                .nome("Obra Concluída")
                .orcamentoPrevisto(new BigDecimal("50000.00"))
                .dataInicio(LocalDate.of(2026, 1, 1))
                .previsaoFim(LocalDate.of(2026, 12, 31))
                .dataConclusao(LocalDate.of(2026, 10, 1))
                .status(StatusObra.CONCLUIDA)
                .progressoPercentual(100)
                .build();

        when(obraRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(obraRepository.save(any(Obra.class))).thenAnswer(inv -> inv.getArgument(0));

        ObraResponseDTO response = obraService.atualizarStatus(
                1L, new ObraStatusUpdateDTO(StatusObra.EM_ANDAMENTO, 40, null));

        assertThat(response.status()).isEqualTo(StatusObra.EM_ANDAMENTO);
        assertThat(response.dataConclusao()).isNull();
        assertThat(response.progressoPercentual()).isEqualTo(40);
    }

    @Test
    void deveCalcularResumoFinanceiroZeradoQuandoObraNaoPossuiLancamentos() {
        Obra obra = Obra.builder()
                .id(1L)
                .nome("Obra Sem Lançamentos")
                .orcamentoPrevisto(new BigDecimal("10000.00"))
                .dataInicio(LocalDate.of(2026, 1, 1))
                .previsaoFim(LocalDate.of(2026, 12, 31))
                .status(StatusObra.EM_ANDAMENTO)
                .progressoPercentual(0)
                .build();

        when(obraRepository.findById(1L)).thenReturn(Optional.of(obra));
        when(lancamentoRepository.sumByObraAndTipoAndStatus(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(lancamentoRepository.countByObraIdAndStatusPagamentoNot(any(), any())).thenReturn(0L);
        when(lancamentoRepository.findDespesasPorCentroCustoByObra(1L)).thenReturn(List.of());

        ResumoFinanceiroObraDTO resumo = obraService.buscarResumoFinanceiro(1L);

        assertThat(resumo.saldoRealizado()).isEqualByComparingTo("0.00");
        assertThat(resumo.saldoProjetado()).isEqualByComparingTo("0.00");
        assertThat(resumo.orcamentoDisponivel()).isEqualByComparingTo("10000.00");
        assertThat(resumo.percentualOrcamentoUtilizado()).isEqualByComparingTo("0.00");
        assertThat(resumo.orcamentoExcedido()).isFalse();
        assertThat(resumo.quantidadeLancamentos()).isZero();
        assertThat(resumo.despesasPorCentroCusto()).isEmpty();
    }
}
