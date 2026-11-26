package com.raizes.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.raizes.api.model.Fidelidade;

@Repository
public interface FidelidadeRepository extends JpaRepository<Fidelidade, Long> {

	List<Fidelidade> findByUsuarioId(Long idUsuario);
}
