package com.raizes.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.raizes.api.model.Estoque;
import com.raizes.api.repository.EstoqueRepository;
import com.raizes.api.repository.ProdutoRepository;
import com.raizes.api.repository.UnidadeRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/estoques")
public class EstoqueController {

	private final EstoqueRepository repository;
	private final ProdutoRepository produtoRepository;
	private final UnidadeRepository unidadeRepository;

	EstoqueController(EstoqueRepository repository, ProdutoRepository produtoRepository,
			UnidadeRepository unidadeRepository) {
		this.repository = repository;
		this.produtoRepository = produtoRepository;
		this.unidadeRepository = unidadeRepository;
	}

	@GetMapping
	public List<Estoque> findAll(@RequestParam(required = false) Long unidade) {
		return unidade == null ? repository.findAll() : repository.findByUnidadeId(unidade);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Estoque> findById(@PathVariable long id) {
		return repository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Estoque create(@Valid @RequestBody Estoque estoque) {
		estoque.setId(null);
		carregarRelacionamentos(estoque);
		return repository.save(estoque);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Estoque> update(@PathVariable long id, @Valid @RequestBody Estoque estoque) {
		return repository.findById(id).map(record -> {
			carregarRelacionamentos(estoque);
			record.setProduto(estoque.getProduto());
			record.setUnidade(estoque.getUnidade());
			record.setQuantidadeEstoque(estoque.getQuantidadeEstoque());
			record.setAtivo(estoque.getAtivo());
			return ResponseEntity.ok(repository.save(record));
		}).orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable long id) {
		return repository.findById(id).map(record -> {
			record.setAtivo(false);
			repository.save(record);
			return ResponseEntity.noContent().<Void>build();
		}).orElse(ResponseEntity.notFound().build());
	}

	private void carregarRelacionamentos(Estoque estoque) {
		estoque.setProduto(produtoRepository.findById(estoque.getProduto().getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto nao encontrado")));
		estoque.setUnidade(unidadeRepository.findById(estoque.getUnidade().getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unidade nao encontrada")));
	}
}
