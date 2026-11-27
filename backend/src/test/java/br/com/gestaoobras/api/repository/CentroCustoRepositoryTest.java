package br.com.gestaoobras.api.repository;

import br.com.gestaoobras.api.domain.entity.CentroCusto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CentroCustoRepositoryTest {

    @Autowired
    private CentroCustoRepository centroCustoRepository;

    @Test
    void deveListarApenasAtivosOrdenadosPorNome() {
        centroCustoRepository.saveAndFlush(CentroCusto.builder()
                .nome("Centro Inativo Repo Teste")
                .ativo(false)
                .build());

        List<CentroCusto> ativos = centroCustoRepository.findAllByAtivoTrueOrderByNomeAsc();

        assertThat(ativos).isNotEmpty();
        assertThat(ativos).allMatch(CentroCusto::getAtivo);
        assertThat(ativos).extracting(CentroCusto::getNome).isSorted();
        assertThat(ativos).extracting(CentroCusto::getNome).doesNotContain("Centro Inativo Repo Teste");
    }

    @Test
    void deveListarTodosOrdenadosPorNome() {
        List<CentroCusto> todos = centroCustoRepository.findAllByOrderByNomeAsc();

        assertThat(todos).isNotEmpty();
        assertThat(todos).extracting(CentroCusto::getNome).isSorted();
    }

    @Test
    void deveVerificarNomeIgnorandoCaixa() {
        CentroCusto salvo = centroCustoRepository.saveAndFlush(CentroCusto.builder()
                .nome("Teste Repositorio")
                .ativo(true)
                .build());

        assertThat(centroCustoRepository.existsByNomeIgnoreCase("teste repositorio")).isTrue();
        assertThat(centroCustoRepository.existsByNomeIgnoreCaseAndIdNot("teste repositorio", salvo.getId())).isFalse();
        assertThat(centroCustoRepository.existsByNomeIgnoreCaseAndIdNot("teste repositorio", -1L)).isTrue();
    }
}
