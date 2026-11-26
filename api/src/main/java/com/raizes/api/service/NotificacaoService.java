package com.raizes.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificacaoService {

	private static final Logger log = LoggerFactory.getLogger(NotificacaoService.class);

	public void enviarEmail(String para, String assunto, String mensagem) {
		log.info("[E-MAIL SIMULADO] Para: {} | Assunto: {} | {}", para, assunto, mensagem);
	}
}
