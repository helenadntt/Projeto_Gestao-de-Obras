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
import br.com.gestaoobras.api.exception.ConflictException;
import br.com.gestaoobras.api.mapper.LancamentoMapper;
import br.com.gestaoobras.api.repository.LancamentoFinanceiroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
}
