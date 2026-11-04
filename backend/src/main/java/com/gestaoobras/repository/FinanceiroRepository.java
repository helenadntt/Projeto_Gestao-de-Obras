package com.gestaoobras.repository;

import com.gestaoobras.model.Financeiro;
import com.gestaoobras.model.TipoFinanceiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinanceiroRepository extends JpaRepository<Financeiro, Long> {
    List<Financeiro> findByObraId(Long obraId);
    List<Financeiro> findByObraIdAndTipo(Long obraId, TipoFinanceiro tipo);
}
