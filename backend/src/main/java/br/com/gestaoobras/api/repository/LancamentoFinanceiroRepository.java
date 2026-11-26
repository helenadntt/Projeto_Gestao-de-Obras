package br.com.gestaoobras.api.repository;

import br.com.gestaoobras.api.domain.entity.LancamentoFinanceiro;
import br.com.gestaoobras.api.domain.enums.StatusPagamento;
import br.com.gestaoobras.api.domain.enums.TipoLancamento;
import br.com.gestaoobras.api.dto.dashboard.GastoPorCentroCustoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface LancamentoFinanceiroRepository
        extends JpaRepository<LancamentoFinanceiro, Long>, JpaSpecificationExecutor<LancamentoFinanceiro> {

    @Override
    @EntityGraph(attributePaths = {"obra", "centroCusto"})
    Page<LancamentoFinanceiro> findAll(@Nullable Specification<LancamentoFinanceiro> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"obra", "centroCusto"})
    Optional<LancamentoFinanceiro> findWithDetailsById(Long id);

    boolean existsByObraId(Long obraId);

    boolean existsByCentroCustoId(Long centroCustoId);

    long countByObraIdAndStatusPagamentoNot(Long obraId, StatusPagamento statusPagamento);

    @Query("""
            SELECT COALESCE(SUM(l.valor), 0)
            FROM LancamentoFinanceiro l
            WHERE l.obra.id = :obraId
              AND l.tipo = :tipo
              AND l.statusPagamento = :status
            """)
    BigDecimal sumByObraAndTipoAndStatus(
            @Param("obraId") Long obraId,
            @Param("tipo") TipoLancamento tipo,
            @Param("status") StatusPagamento status
    );

    @Query("""
            SELECT COALESCE(SUM(l.valor), 0)
            FROM LancamentoFinanceiro l
            WHERE l.tipo = :tipo
              AND l.statusPagamento = :status
            """)
    BigDecimal sumGlobalByTipoAndStatus(
            @Param("tipo") TipoLancamento tipo,
            @Param("status") StatusPagamento status
    );

    @Query("""
            SELECT new br.com.gestaoobras.api.dto.dashboard.GastoPorCentroCustoDTO(
                cc.id,
                COALESCE(cc.nome, 'Sem Centro de Custo'),
                SUM(l.valor),
                COUNT(l.id)
            )
            FROM LancamentoFinanceiro l
            LEFT JOIN l.centroCusto cc
            WHERE l.obra.id = :obraId
              AND l.tipo = br.com.gestaoobras.api.domain.enums.TipoLancamento.DESPESA
              AND l.statusPagamento <> br.com.gestaoobras.api.domain.enums.StatusPagamento.CANCELADO
            GROUP BY cc.id, cc.nome
            ORDER BY SUM(l.valor) DESC
            """)
    List<GastoPorCentroCustoDTO> findDespesasPorCentroCustoByObra(@Param("obraId") Long obraId);

    @Query("""
            SELECT new br.com.gestaoobras.api.dto.dashboard.GastoPorCentroCustoDTO(
                cc.id,
                COALESCE(cc.nome, 'Sem Centro de Custo'),
                SUM(l.valor),
                COUNT(l.id)
            )
            FROM LancamentoFinanceiro l
            LEFT JOIN l.centroCusto cc
            WHERE l.tipo = br.com.gestaoobras.api.domain.enums.TipoLancamento.DESPESA
              AND l.statusPagamento <> br.com.gestaoobras.api.domain.enums.StatusPagamento.CANCELADO
            GROUP BY cc.id, cc.nome
            ORDER BY SUM(l.valor) DESC
            """)
    List<GastoPorCentroCustoDTO> findDespesasPorCentroCustoGlobal();

    @Query("""
            SELECT l.obra.id, COALESCE(SUM(l.valor), 0)
            FROM LancamentoFinanceiro l
            WHERE l.tipo = br.com.gestaoobras.api.domain.enums.TipoLancamento.DESPESA
              AND l.statusPagamento <> br.com.gestaoobras.api.domain.enums.StatusPagamento.CANCELADO
            GROUP BY l.obra.id
            """)
    List<Object[]> sumDespesasAtivasAgrupadasPorObra();

    @Query("""
            SELECT l.obra.id, COALESCE(SUM(l.valor), 0)
            FROM LancamentoFinanceiro l
            WHERE l.tipo = br.com.gestaoobras.api.domain.enums.TipoLancamento.DESPESA
              AND l.statusPagamento = br.com.gestaoobras.api.domain.enums.StatusPagamento.PAGO
            GROUP BY l.obra.id
            """)
    List<Object[]> sumDespesasPagasAgrupadasPorObra();

    @Modifying
    @Query("UPDATE LancamentoFinanceiro l SET l.centroCusto = NULL WHERE l.centroCusto.id = :centroCustoId")
    void desvincularCentroCusto(@Param("centroCustoId") Long centroCustoId);
}
