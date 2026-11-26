package br.com.gestaoobras.api.repository;

import br.com.gestaoobras.api.domain.entity.CentroCusto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CentroCustoRepository extends JpaRepository<CentroCusto, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    List<CentroCusto> findAllByAtivoTrueOrderByNomeAsc();

    List<CentroCusto> findAllByOrderByNomeAsc();
}
