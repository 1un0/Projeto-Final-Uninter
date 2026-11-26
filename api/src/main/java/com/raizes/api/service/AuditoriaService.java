package com.raizes.api.service;

import org.springframework.stereotype.Service;

import com.raizes.api.model.Auditoria;
import com.raizes.api.model.enums.AcaoAuditoria;
import com.raizes.api.repository.AuditoriaRepository;
import com.raizes.api.repository.UsuarioRepository;

@Service
public class AuditoriaService {

	private final AuditoriaRepository auditoriaRepository;
	private final UsuarioRepository usuarioRepository;

	AuditoriaService(AuditoriaRepository auditoriaRepository, UsuarioRepository usuarioRepository) {
		this.auditoriaRepository = auditoriaRepository;
		this.usuarioRepository = usuarioRepository;
	}

	public void registrar(Long idUsuario, AcaoAuditoria acao, String entidade, Long idRegistro, String detalhes) {
		Auditoria auditoria = new Auditoria();
		if (idUsuario != null) {
			auditoria.setUsuario(usuarioRepository.getReferenceById(idUsuario));
		}
		auditoria.setAcao(acao);
		auditoria.setEntidade(entidade);
		auditoria.setIdRegistro(idRegistro);
		auditoria.setDetalhes(detalhes);
		auditoriaRepository.save(auditoria);
	}
}
