package com.raizes.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.raizes.api.model.Estoque;

@Repository
public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

	List<Estoque> findByUnidadeId(Long idUnidade);

	Optional<Estoque> findByProdutoIdAndUnidadeIdAndAtivoTrue(Long idProduto, Long idUnidade);
}
