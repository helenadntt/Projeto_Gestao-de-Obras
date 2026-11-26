package br.com.gestaoobras.api.service;

import br.com.gestaoobras.api.domain.entity.Obra;
import br.com.gestaoobras.api.domain.enums.StatusObra;
import br.com.gestaoobras.api.dto.obra.ObraRequestDTO;
import br.com.gestaoobras.api.dto.obra.ObraResponseDTO;
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

import java.math.BigDecimal;
import java.time.LocalDate;
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
}
