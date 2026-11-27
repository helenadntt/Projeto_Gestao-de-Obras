package br.com.gestaoobras.api.service;

import br.com.gestaoobras.api.domain.entity.CentroCusto;
import br.com.gestaoobras.api.dto.centrocusto.CentroCustoRequestDTO;
import br.com.gestaoobras.api.dto.centrocusto.CentroCustoResponseDTO;
import br.com.gestaoobras.api.exception.ConflictException;
import br.com.gestaoobras.api.exception.ResourceNotFoundException;
import br.com.gestaoobras.api.mapper.CentroCustoMapper;
import br.com.gestaoobras.api.repository.CentroCustoRepository;
import br.com.gestaoobras.api.repository.LancamentoFinanceiroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CentroCustoServiceTest {

    @Mock
    private CentroCustoRepository centroCustoRepository;

    @Mock
    private LancamentoFinanceiroRepository lancamentoRepository;

    @Spy
    private CentroCustoMapper mapper = new CentroCustoMapper();

    @InjectMocks
    private CentroCustoService centroCustoService;

    @Test
    void deveCriarCentroCustoComSucesso() {
        CentroCustoRequestDTO dto = new CentroCustoRequestDTO("Elétrica", "Fiação e quadros", true);
        when(centroCustoRepository.existsByNomeIgnoreCase("Elétrica")).thenReturn(false);
        when(centroCustoRepository.save(any(CentroCusto.class))).thenAnswer(inv -> {
            CentroCusto c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        CentroCustoResponseDTO response = centroCustoService.criar(dto);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.nome()).isEqualTo("Elétrica");
        assertThat(response.ativo()).isTrue();
    }

    @Test
    void deveLancarConflictAoCriarCentroComNomeDuplicado() {
        CentroCustoRequestDTO dto = new CentroCustoRequestDTO("Material", null, true);
        when(centroCustoRepository.existsByNomeIgnoreCase("Material")).thenReturn(true);

        assertThatThrownBy(() -> centroCustoService.criar(dto))
                .isInstanceOf(ConflictException.class);

        verify(centroCustoRepository, never()).save(any());
    }

    @Test
    void deveExcluirCentroSemLancamentos() {
        CentroCusto centro = CentroCusto.builder().id(5L).nome("Temporário").build();
        when(centroCustoRepository.findById(5L)).thenReturn(Optional.of(centro));
        when(lancamentoRepository.existsByCentroCustoId(5L)).thenReturn(false);

        centroCustoService.excluir(5L);

        verify(centroCustoRepository).delete(centro);
    }

    @Test
    void deveLancarConflictSugerindoInativarAoExcluirCentroComLancamentos() {
        CentroCusto centro = CentroCusto.builder().id(5L).nome("Mão de Obra").build();
        when(centroCustoRepository.findById(5L)).thenReturn(Optional.of(centro));
        when(lancamentoRepository.existsByCentroCustoId(5L)).thenReturn(true);

        assertThatThrownBy(() -> centroCustoService.excluir(5L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("inativ");

        verify(centroCustoRepository, never()).delete(any());
    }

    @Test
    void deveListarApenasCentrosAtivosQuandoSolicitado() {
        when(centroCustoRepository.findAllByAtivoTrueOrderByNomeAsc())
                .thenReturn(List.of(CentroCusto.builder().id(1L).nome("Material").ativo(true).build()));

        List<CentroCustoResponseDTO> lista = centroCustoService.listar(true);

        assertThat(lista).hasSize(1);
        assertThat(lista.getFirst().nome()).isEqualTo("Material");
        verify(centroCustoRepository).findAllByAtivoTrueOrderByNomeAsc();
    }

    @Test
    void deveListarTodosOsCentrosQuandoNaoFiltrarPorAtivos() {
        when(centroCustoRepository.findAllByOrderByNomeAsc())
                .thenReturn(List.of(
                        CentroCusto.builder().id(1L).nome("Material").ativo(true).build(),
                        CentroCusto.builder().id(2L).nome("Inativo").ativo(false).build()
                ));

        List<CentroCustoResponseDTO> lista = centroCustoService.listar(false);

        assertThat(lista).hasSize(2);
        verify(centroCustoRepository).findAllByOrderByNomeAsc();
    }

    @Test
    void deveBuscarCentroPorId() {
        when(centroCustoRepository.findById(7L))
                .thenReturn(Optional.of(CentroCusto.builder().id(7L).nome("Equipamentos").ativo(true).build()));

        CentroCustoResponseDTO response = centroCustoService.buscarPorId(7L);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.nome()).isEqualTo("Equipamentos");
    }

    @Test
    void deveLancarNotFoundAoBuscarCentroInexistente() {
        when(centroCustoRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> centroCustoService.buscarPorId(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    void deveAtualizarCentroCusto() {
        CentroCusto existente = CentroCusto.builder().id(5L).nome("Elétrica").ativo(true).build();

        when(centroCustoRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(centroCustoRepository.existsByNomeIgnoreCaseAndIdNot("Elétrica e Hidráulica", 5L)).thenReturn(false);
        when(centroCustoRepository.save(any(CentroCusto.class))).thenAnswer(inv -> inv.getArgument(0));

        CentroCustoResponseDTO response = centroCustoService.atualizar(
                5L, new CentroCustoRequestDTO("Elétrica e Hidráulica", "Instalações", false));

        assertThat(response.nome()).isEqualTo("Elétrica e Hidráulica");
        assertThat(response.ativo()).isFalse();
    }

    @Test
    void deveLancarConflictAoAtualizarParaNomeJaExistente() {
        CentroCusto existente = CentroCusto.builder().id(5L).nome("Elétrica").ativo(true).build();

        when(centroCustoRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(centroCustoRepository.existsByNomeIgnoreCaseAndIdNot("Material", 5L)).thenReturn(true);

        assertThatThrownBy(() -> centroCustoService.atualizar(
                5L, new CentroCustoRequestDTO("Material", null, true)))
                .isInstanceOf(ConflictException.class);

        verify(centroCustoRepository, never()).save(any());
    }

    @Test
    void deveLancarNotFoundAoExcluirCentroInexistente() {
        when(centroCustoRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> centroCustoService.excluir(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");

        verify(centroCustoRepository, never()).delete(any());
    }
}
