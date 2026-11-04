package com.gestaoobras.repository;

import com.gestaoobras.model.Obra;
import com.gestaoobras.model.StatusObra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObraRepository extends JpaRepository<Obra, Long> {
    List<Obra> findByStatus(StatusObra status);
}
