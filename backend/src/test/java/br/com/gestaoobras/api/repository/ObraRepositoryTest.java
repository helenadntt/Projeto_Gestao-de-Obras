package br.com.gestaoobras.api.repository;

import br.com.gestaoobras.api.domain.entity.Obra;
import br.com.gestaoobras.api.domain.enums.StatusObra;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ObraRepositoryTest {

    @Autowired
    private ObraRepository obraRepository;

    @Test
    void deveRetornarPaginaVaziaQuandoFiltroNaoEncontrarObras() {
        Specification<Obra> spec = (root, query, cb) ->
                cb.like(cb.lower(root.get("nome")), "%obra-inexistente-repo-test%");

        Page<Obra> pagina = obraRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(pagina.getTotalElements()).isZero();
        assertThat(pagina.getContent()).isEmpty();
    }

    @Test
    void devePaginarObrasOrdenadasPorNome() {
        obraRepository.saveAll(List.of(
                obra("Repo Teste Beta", StatusObra.EM_ANDAMENTO),
                obra("Repo Teste Alfa", StatusObra.PLANEJAMENTO)
        ));

        Specification<Obra> spec = (root, query, cb) ->
                cb.like(cb.lower(root.get("nome")), "repo teste%");

        Page<Obra> pagina = obraRepository.findAll(
                spec, PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "nome")));

        assertThat(pagina.getTotalElements()).isEqualTo(2);
        assertThat(pagina.getContent()).extracting(Obra::getNome)
                .containsExactly("Repo Teste Alfa", "Repo Teste Beta");
    }

    @Test
    void deveFiltrarObrasPorStatusEBuscaTextualComSpecification() {
        obraRepository.saveAll(List.of(
                obra("Repo Teste Sol", StatusObra.EM_ANDAMENTO),
                obra("Repo Teste Norte", StatusObra.EM_ANDAMENTO),
                obra("Repo Teste Lua", StatusObra.PLANEJAMENTO)
        ));

        Specification<Obra> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("status"), StatusObra.EM_ANDAMENTO),
                cb.like(cb.lower(root.get("nome")), "%repo teste sol%")
        );

        List<Obra> encontradas = obraRepository.findAll(spec);

        assertThat(encontradas).hasSize(1);
        assertThat(encontradas.getFirst().getNome()).isEqualTo("Repo Teste Sol");
    }

    private Obra obra(String nome, StatusObra status) {
        return Obra.builder()
                .nome(nome)
                .endereco("Rua de Teste, 10")
                .responsavel("Eng. Teste")
                .orcamentoPrevisto(new BigDecimal("50000.00"))
                .dataInicio(LocalDate.of(2026, 1, 1))
                .previsaoFim(LocalDate.of(2026, 12, 31))
                .status(status)
                .progressoPercentual(0)
                .build();
    }
}
