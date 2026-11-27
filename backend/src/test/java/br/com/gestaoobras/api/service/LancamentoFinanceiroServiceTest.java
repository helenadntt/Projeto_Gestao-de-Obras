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
class LancamentoFinanceiroServiceTest {

    @Mock
    private LancamentoFinanceiroRepository lancamentoRepository;

    @Mock
    private ObraService obraService;

    @Mock
    private CentroCustoService centroCustoService;

    @Spy
    private LancamentoMapper mapper = new LancamentoMapper();

    @InjectMocks
    private LancamentoFinanceiroService lancamentoService;

    @Test
    void deveMarcarComoPagoEReverterParaPendenteLimpandoDataPagamento() {
        Obra obra = Obra.builder().id(1L).nome("Obra Alpha").status(StatusObra.EM_ANDAMENTO).build();
        LancamentoFinanceiro entity = LancamentoFinanceiro.builder()
                .id(10L)
                .obra(obra)
                .descricao("Cimento")
                .tipo(TipoLancamento.DESPESA)
                .valor(new BigDecimal("1500.00"))
                .dataCompetencia(LocalDate.of(2026, 5, 1))
                .statusPagamento(StatusPagamento.PENDENTE)
                .build();

        when(lancamentoRepository.findWithDetailsById(10L)).thenReturn(Optional.of(entity));
        when(lancamentoRepository.save(any(LancamentoFinanceiro.class))).thenAnswer(inv -> inv.getArgument(0));

        // Marcar como PAGO sem informar data -> preenche automaticamente
        LancamentoResponseDTO pago = lancamentoService.atualizarPagamento(
                10L, new LancamentoPagamentoUpdateDTO(StatusPagamento.PAGO, null));
        assertThat(pago.statusPagamento()).isEqualTo(StatusPagamento.PAGO);
        assertThat(pago.dataPagamento()).isNotNull();

        // Reverter para PENDENTE -> limpa dataPagamento
        LancamentoResponseDTO pendente = lancamentoService.atualizarPagamento(
                10L, new LancamentoPagamentoUpdateDTO(StatusPagamento.PENDENTE, null));
        assertThat(pendente.statusPagamento()).isEqualTo(StatusPagamento.PENDENTE);
        assertThat(pendente.dataPagamento()).isNull();
    }

    @Test
    void deveLancarConflictAoCriarLancamentoEmObraCancelada() {
        Obra cancelada = Obra.builder().id(2L).nome("Cancelada").status(StatusObra.CANCELADA).build();
        when(obraService.buscarEntidade(2L)).thenReturn(cancelada);

        LancamentoRequestDTO dto = new LancamentoRequestDTO(
                2L, null, "Teste", TipoLancamento.DESPESA, new BigDecimal("100.00"),
                LocalDate.now(), null, StatusPagamento.PENDENTE, null, null
        );

        assertThatThrownBy(() -> lancamentoService.criar(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("cancelada");
    }

    @Test
    void deveLancarConflictAoCriarLancamentoEmCentroCustoInativo() {
        Obra ativa = Obra.builder().id(1L).nome("Ativa").status(StatusObra.EM_ANDAMENTO).build();
        CentroCusto inativo = CentroCusto.builder().id(3L).nome("Inativo").ativo(false).build();

        when(obraService.buscarEntidade(1L)).thenReturn(ativa);
        when(centroCustoService.buscarEntidade(3L)).thenReturn(inativo);

        LancamentoRequestDTO dto = new LancamentoRequestDTO(
                1L, 3L, "Teste", TipoLancamento.DESPESA, new BigDecimal("100.00"),
                LocalDate.now(), null, StatusPagamento.PENDENTE, null, null
        );

        assertThatThrownBy(() -> lancamentoService.criar(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("inativo");
    }

    @Test
    void deveCriarLancamentoPendenteSemDataPagamento() {
        Obra obra = Obra.builder().id(1L).nome("Obra Alpha").status(StatusObra.EM_ANDAMENTO).build();
        CentroCusto centro = CentroCusto.builder().id(2L).nome("Material").ativo(true).build();

        when(obraService.buscarEntidade(1L)).thenReturn(obra);
        when(centroCustoService.buscarEntidade(2L)).thenReturn(centro);
        when(lancamentoRepository.save(any(LancamentoFinanceiro.class))).thenAnswer(inv -> {
            LancamentoFinanceiro l = inv.getArgument(0);
            l.setId(99L);
            return l;
        });

        LancamentoRequestDTO dto = new LancamentoRequestDTO(
                1L, 2L, "Cimento", TipoLancamento.DESPESA, new BigDecimal("500.00"),
                LocalDate.of(2026, 5, 1), null, StatusPagamento.PENDENTE, null, null
        );

        LancamentoResponseDTO response = lancamentoService.criar(dto);

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.obraNome()).isEqualTo("Obra Alpha");
        assertThat(response.centroCustoNome()).isEqualTo("Material");
        assertThat(response.statusPagamento()).isEqualTo(StatusPagamento.PENDENTE);
        assertThat(response.dataPagamento()).isNull();
    }

    @Test
    void deveCriarLancamentoPagoPreenchendoDataPagamentoAutomaticamente() {
        Obra obra = Obra.builder().id(1L).nome("Obra Alpha").status(StatusObra.EM_ANDAMENTO).build();
        CentroCusto centro = CentroCusto.builder().id(2L).nome("Material").ativo(true).build();

        when(obraService.buscarEntidade(1L)).thenReturn(obra);
        when(centroCustoService.buscarEntidade(2L)).thenReturn(centro);
        when(lancamentoRepository.save(any(LancamentoFinanceiro.class))).thenAnswer(inv -> inv.getArgument(0));

        LancamentoRequestDTO dto = new LancamentoRequestDTO(
                1L, 2L, "Cimento", TipoLancamento.DESPESA, new BigDecimal("500.00"),
                LocalDate.of(2026, 5, 1), null, StatusPagamento.PAGO, null, null
        );

        LancamentoResponseDTO response = lancamentoService.criar(dto);

        assertThat(response.statusPagamento()).isEqualTo(StatusPagamento.PAGO);
        assertThat(response.dataPagamento()).isEqualTo(LocalDate.now());
    }

    @Test
    void deveAtualizarLancamentoExistente() {
        Obra obra = Obra.builder().id(1L).nome("Obra Alpha").status(StatusObra.EM_ANDAMENTO).build();
        CentroCusto centro = CentroCusto.builder().id(2L).nome("Material").ativo(true).build();
        LancamentoFinanceiro existente = LancamentoFinanceiro.builder()
                .id(10L)
                .obra(obra)
                .centroCusto(centro)
                .descricao("Descrição antiga")
                .tipo(TipoLancamento.DESPESA)
                .valor(new BigDecimal("100.00"))
                .dataCompetencia(LocalDate.of(2026, 5, 1))
                .statusPagamento(StatusPagamento.PENDENTE)
                .build();

        when(lancamentoRepository.findWithDetailsById(10L)).thenReturn(Optional.of(existente));
        when(obraService.buscarEntidade(1L)).thenReturn(obra);
        when(centroCustoService.buscarEntidade(2L)).thenReturn(centro);
        when(lancamentoRepository.save(any(LancamentoFinanceiro.class))).thenAnswer(inv -> inv.getArgument(0));

        LancamentoRequestDTO dto = new LancamentoRequestDTO(
                1L, 2L, "Descrição nova", TipoLancamento.RECEITA, new BigDecimal("750.00"),
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2), StatusPagamento.PAGO, null, null
        );

        LancamentoResponseDTO response = lancamentoService.atualizar(10L, dto);

        assertThat(response.descricao()).isEqualTo("Descrição nova");
        assertThat(response.tipo()).isEqualTo(TipoLancamento.RECEITA);
        assertThat(response.valor()).isEqualByComparingTo("750.00");
        assertThat(response.dataPagamento()).isEqualTo(LocalDate.of(2026, 6, 2));
    }

    @Test
    void deveBuscarLancamentoPorId() {
        Obra obra = Obra.builder().id(1L).nome("Obra Alpha").status(StatusObra.EM_ANDAMENTO).build();
        LancamentoFinanceiro entity = LancamentoFinanceiro.builder()
                .id(10L)
                .obra(obra)
                .descricao("Cimento")
                .tipo(TipoLancamento.DESPESA)
                .valor(new BigDecimal("100.00"))
                .dataCompetencia(LocalDate.of(2026, 5, 1))
                .statusPagamento(StatusPagamento.PENDENTE)
                .build();

        when(lancamentoRepository.findWithDetailsById(10L)).thenReturn(Optional.of(entity));

        LancamentoResponseDTO response = lancamentoService.buscarPorId(10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.descricao()).isEqualTo("Cimento");
    }

    @Test
    void deveLancarNotFoundAoBuscarLancamentoInexistente() {
        when(lancamentoRepository.findWithDetailsById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lancamentoService.buscarPorId(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    void deveLancarBusinessRuleQuandoFiltroDePeriodoForInvalido() {
        assertThatThrownBy(() -> lancamentoService.listar(
                null, null, null, null,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 5, 1),
                PageRequest.of(0, 10)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("data final");
    }

    @Test
    void deveListarLancamentosDaObra() {
        Obra obra = Obra.builder().id(1L).nome("Obra Alpha").status(StatusObra.EM_ANDAMENTO).build();
        LancamentoFinanceiro entity = LancamentoFinanceiro.builder()
                .id(10L)
                .obra(obra)
                .descricao("Cimento")
                .tipo(TipoLancamento.DESPESA)
                .valor(new BigDecimal("100.00"))
                .dataCompetencia(LocalDate.of(2026, 5, 1))
                .statusPagamento(StatusPagamento.PENDENTE)
                .build();

        when(obraService.buscarEntidade(1L)).thenReturn(obra);
        when(lancamentoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        Page<LancamentoResponseDTO> pagina = lancamentoService.listarPorObra(
                1L, TipoLancamento.DESPESA, StatusPagamento.PENDENTE, PageRequest.of(0, 10));

        assertThat(pagina.getTotalElements()).isEqualTo(1);
        assertThat(pagina.getContent().getFirst().descricao()).isEqualTo("Cimento");
    }

    @Test
    void deveExcluirLancamento() {
        Obra obra = Obra.builder().id(1L).nome("Obra Alpha").status(StatusObra.EM_ANDAMENTO).build();
        LancamentoFinanceiro entity = LancamentoFinanceiro.builder().id(10L).obra(obra).build();

        when(lancamentoRepository.findWithDetailsById(10L)).thenReturn(Optional.of(entity));

        lancamentoService.excluir(10L);

        verify(lancamentoRepository).delete(entity);
    }

    @Test
    void deveLancarNotFoundAoExcluirLancamentoInexistente() {
        when(lancamentoRepository.findWithDetailsById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lancamentoService.excluir(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");

        verify(lancamentoRepository, never()).delete(any(LancamentoFinanceiro.class));
    }

    @Test
    void deveLimparDataPagamentoAoCancelarLancamento() {
        Obra obra = Obra.builder().id(1L).nome("Obra Alpha").status(StatusObra.EM_ANDAMENTO).build();
        LancamentoFinanceiro entity = LancamentoFinanceiro.builder()
                .id(10L)
                .obra(obra)
                .descricao("Cimento")
                .tipo(TipoLancamento.DESPESA)
                .valor(new BigDecimal("100.00"))
                .dataCompetencia(LocalDate.of(2026, 5, 1))
                .dataPagamento(LocalDate.of(2026, 5, 2))
                .statusPagamento(StatusPagamento.PAGO)
                .build();

        when(lancamentoRepository.findWithDetailsById(10L)).thenReturn(Optional.of(entity));
        when(lancamentoRepository.save(any(LancamentoFinanceiro.class))).thenAnswer(inv -> inv.getArgument(0));

        LancamentoResponseDTO response = lancamentoService.atualizarPagamento(
                10L, new LancamentoPagamentoUpdateDTO(StatusPagamento.CANCELADO, LocalDate.of(2026, 5, 3)));

        assertThat(response.statusPagamento()).isEqualTo(StatusPagamento.CANCELADO);
        assertThat(response.dataPagamento()).isNull();
    }
}
