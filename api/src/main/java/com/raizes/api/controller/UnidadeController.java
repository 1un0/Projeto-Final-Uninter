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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.raizes.api.model.Unidade;
import com.raizes.api.repository.UnidadeRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/unidades")
public class UnidadeController {

	private final UnidadeRepository repository;

	UnidadeController(UnidadeRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	public List<Unidade> findAll() {
		return repository.findAll();
	}

	@GetMapping("/{id}")
	public ResponseEntity<Unidade> findById(@PathVariable long id) {
		return repository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Unidade create(@Valid @RequestBody Unidade unidade) {
		unidade.setId(null);
		return repository.save(unidade);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Unidade> update(@PathVariable long id, @Valid @RequestBody Unidade unidade) {
		return repository.findById(id).map(record -> {
			record.setNomeUnidade(unidade.getNomeUnidade());
			record.setEndereco(unidade.getEndereco());
			record.setAtivo(unidade.getAtivo());
			return ResponseEntity.ok(repository.save(record));
		}).orElse(ResponseEntity.notFound().build());
	}

	// deleta e deixa registro
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable long id) {
		return repository.findById(id).map(record -> {
			record.setAtivo(false);
			repository.save(record);
			return ResponseEntity.noContent().<Void>build();
		}).orElse(ResponseEntity.notFound().build());
	}
}
