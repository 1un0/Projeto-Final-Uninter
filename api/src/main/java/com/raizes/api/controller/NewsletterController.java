package com.raizes.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raizes.api.dto.NewsletterRequest;
import com.raizes.api.model.Usuario;
import com.raizes.api.model.enums.AcaoAuditoria;
import com.raizes.api.repository.UsuarioRepository;
import com.raizes.api.security.UsuarioAutenticado;
import com.raizes.api.service.AuditoriaService;
import com.raizes.api.service.NotificacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/newsletter")
public class NewsletterController {

	private final UsuarioRepository usuarioRepository;
	private final NotificacaoService notificacao;
	private final AuditoriaService auditoria;

	NewsletterController(UsuarioRepository usuarioRepository, NotificacaoService notificacao,
			AuditoriaService auditoria) {
		this.usuarioRepository = usuarioRepository;
		this.notificacao = notificacao;
		this.auditoria = auditoria;
	}

	@PostMapping("/envio")
	public Map<String, Object> enviar(@Valid @RequestBody NewsletterRequest req,
			@AuthenticationPrincipal UsuarioAutenticado logado) {
		List<Usuario> inscritos = usuarioRepository.findByAceitaNewsletterTrueAndContaAtivaTrue();
		for (Usuario u : inscritos) {
			notificacao.enviarEmail(u.getEmail(), req.assunto(), req.mensagem());
		}
		auditoria.registrar(logado.id(), AcaoAuditoria.CRIACAO, "Newsletter", null,
				"Newsletter \"" + req.assunto() + "\" enviada para " + inscritos.size() + " inscritos");
		return Map.of("enviados", inscritos.size(), "simulado", true);
	}
}
