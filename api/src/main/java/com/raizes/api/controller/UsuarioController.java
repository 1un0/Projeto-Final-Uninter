package com.raizes.api.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.raizes.api.model.Usuario;
import com.raizes.api.repository.UsuarioRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

	private final UsuarioRepository repository;
	private final PasswordEncoder passwordEncoder;

	UsuarioController(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
	}

	@GetMapping
	public List<Usuario> findAll() {
		return repository.findAll();
	}

	@GetMapping("/{id}")
	public ResponseEntity<Usuario> findById(@PathVariable long id) {
		return repository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Usuario create(@Valid @RequestBody Usuario usuario) {
		if (repository.existsByEmail(usuario.getEmail())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail ja cadastrado");
		}
		usuario.setId(null);
		usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
		registrarConsentimento(usuario);
		return repository.save(usuario);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Usuario> update(@PathVariable long id, @RequestBody Usuario usuario) {
		return repository.findById(id).map(record -> {
			if (usuario.getEmail() != null && !usuario.getEmail().equalsIgnoreCase(record.getEmail())
					&& repository.existsByEmail(usuario.getEmail())) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail ja cadastrado");
			}
			if (usuario.getNome() != null) record.setNome(usuario.getNome());
			if (usuario.getEmail() != null) record.setEmail(usuario.getEmail());
			if (usuario.getCpf() != null) record.setCpf(usuario.getCpf());
			if (usuario.getPerfil() != null) record.setPerfil(usuario.getPerfil());
			if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
				record.setSenha(passwordEncoder.encode(usuario.getSenha()));
			}
			if (usuario.getAceitaDados() != null) record.setAceitaDados(usuario.getAceitaDados());
			if (usuario.getAceitaNewsletter() != null) record.setAceitaNewsletter(usuario.getAceitaNewsletter());
			if (usuario.getAceitaFidelidade() != null) record.setAceitaFidelidade(usuario.getAceitaFidelidade());
			registrarConsentimento(record);
			return ResponseEntity.ok(repository.save(record));
		}).orElse(ResponseEntity.notFound().build());
	}

	// desativa a conta
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable long id) {
		return repository.findById(id).map(record -> {
			record.setContaAtiva(false);
			repository.save(record);
			return ResponseEntity.noContent().<Void>build();
		}).orElse(ResponseEntity.notFound().build());
	}

	// testar (tambem usado no /auth/registro)
	static void registrarConsentimento(Usuario usuario) {
		boolean consentiu = Boolean.TRUE.equals(usuario.getAceitaDados())
				|| Boolean.TRUE.equals(usuario.getAceitaNewsletter())
				|| Boolean.TRUE.equals(usuario.getAceitaFidelidade());
		usuario.setConsentimentoData(consentiu ? LocalDateTime.now() : null);
	}
}
