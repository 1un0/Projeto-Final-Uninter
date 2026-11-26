package com.raizes.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.raizes.api.model.Auditoria;
import com.raizes.api.model.Usuario;
import com.raizes.api.model.enums.AcaoAuditoria;
import com.raizes.api.model.enums.Perfil;
import com.raizes.api.repository.AuditoriaRepository;
import com.raizes.api.repository.UsuarioRepository;
import com.raizes.api.security.JwtService;
import com.raizes.api.security.LoginRequest;
import com.raizes.api.security.TokenResponse;
import com.raizes.api.security.UsuarioAutenticado;
import com.raizes.api.service.NotificacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final UsuarioRepository usuarioRepository;
	private final AuditoriaRepository auditoriaRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final NotificacaoService notificacao;

	AuthController(AuthenticationManager authenticationManager, UsuarioRepository usuarioRepository,
			AuditoriaRepository auditoriaRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
			NotificacaoService notificacao) {
		this.authenticationManager = authenticationManager;
		this.usuarioRepository = usuarioRepository;
		this.auditoriaRepository = auditoriaRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.notificacao = notificacao;
	}

	@PostMapping("/login")
	public TokenResponse login(@Valid @RequestBody LoginRequest login) {
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login.email(), login.senha()));
		} catch (DisabledException e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Conta desativada");
		} catch (BadCredentialsException e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha invalidos");
		}
		Usuario usuario = usuarioRepository.findByEmail(login.email()).orElseThrow();

		Auditoria auditoria = new Auditoria();
		auditoria.setUsuario(usuario);
		auditoria.setAcao(AcaoAuditoria.LOGIN);
		auditoria.setEntidade("Usuario");
		auditoria.setIdRegistro(usuario.getId());
		auditoria.setDetalhes("Login realizado");
		auditoriaRepository.save(auditoria);

		return gerarResposta(usuario);
	}

	@PostMapping("/registro")
	@ResponseStatus(HttpStatus.CREATED)
	public TokenResponse registro(@Valid @RequestBody Usuario usuario) {
		if (usuarioRepository.existsByEmail(usuario.getEmail())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail ja cadastrado");
		}
		usuario.setId(null);
		usuario.setPerfil(Perfil.CLIENTE);
		usuario.setContaAtiva(true);
		usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
		UsuarioController.registrarConsentimento(usuario);
		Usuario salvo = usuarioRepository.save(usuario);
		notificacao.enviarEmail(salvo.getEmail(), "Bem-vindo(a) ao Raizes do Nordeste",
				"Ola, " + salvo.getNome() + "! Seu cadastro foi realizado.");
		return gerarResposta(salvo);
	}

	@GetMapping("/me")
	public ResponseEntity<Usuario> me(@AuthenticationPrincipal UsuarioAutenticado logado) {
		return usuarioRepository.findById(logado.id()).map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	private TokenResponse gerarResposta(Usuario usuario) {
		return new TokenResponse(jwtService.gerarToken(usuario), "Bearer", jwtService.getExpiracaoMinutos(),
				usuario.getId(), usuario.getNome(), usuario.getPerfil());
	}
}
