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

import com.raizes.api.model.Produto;
import com.raizes.api.repository.ProdutoRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

	private final ProdutoRepository repository;

	ProdutoController(ProdutoRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	public List<Produto> findAll(@RequestParam(defaultValue = "false") boolean ativos) {
		return ativos ? repository.findByAtivoTrue() : repository.findAll();
	}

	@GetMapping("/{id}")
	public ResponseEntity<Produto> findById(@PathVariable long id) {
		return repository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Produto create(@Valid @RequestBody Produto produto) {
		produto.setId(null);
		return repository.save(produto);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Produto> update(@PathVariable long id, @Valid @RequestBody Produto produto) {
		return repository.findById(id).map(record -> {
			record.setNomeProduto(produto.getNomeProduto());
			record.setValor(produto.getValor());
			record.setSazonal(produto.getSazonal());
			record.setAtivo(produto.getAtivo());
			return ResponseEntity.ok(repository.save(record));
		}).orElse(ResponseEntity.notFound().build());
	}

	// deleta mas deixa salvo o registro
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable long id) {
		return repository.findById(id).map(record -> {
			record.setAtivo(false);
			repository.save(record);
			return ResponseEntity.noContent().<Void>build();
		}).orElse(ResponseEntity.notFound().build());
	}
}
