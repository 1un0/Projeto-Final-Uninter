package com.raizes.api.controller;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raizes.api.model.Auditoria;
import com.raizes.api.repository.AuditoriaRepository;

@RestController
@RequestMapping("/auditoria")
public class AuditoriaController {

	private final AuditoriaRepository repository;

	AuditoriaController(AuditoriaRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	public List<Auditoria> findAll() {
		return repository.findAll(Sort.by(Sort.Direction.DESC, "registroData"));
	}
}
