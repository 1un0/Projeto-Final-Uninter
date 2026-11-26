package com.raizes.api.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.raizes.api.model.Usuario;
import com.raizes.api.model.enums.Perfil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private static final Logger log = LoggerFactory.getLogger(JwtService.class);

	private final SecretKey chave;
	private final long expiracaoMinutos;

	public JwtService(@Value("${app.jwt.secret:}") String segredo,
			@Value("${app.jwt.expiration-minutes:120}") long expiracaoMinutos) {
		if (segredo == null || segredo.isBlank()) {
			log.warn("app.jwt.secret nao definido (variavel JWT_SECRET). Usando chave aleatoria.");
			this.chave = Jwts.SIG.HS256.key().build();
		} else {
			byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
			if (bytes.length < 32) {
				throw new IllegalStateException("app.jwt.secret precisa ter pelo menos 32 caracteres");
			}
			this.chave = Keys.hmacShaKeyFor(bytes);
		}
		this.expiracaoMinutos = expiracaoMinutos;
	}

	public String gerarToken(Usuario usuario) {
		Instant agora = Instant.now();
		return Jwts.builder()
				.subject(usuario.getEmail())
				.claim("id", usuario.getId())
				.claim("perfil", usuario.getPerfil().name())
				.issuedAt(Date.from(agora))
				.expiration(Date.from(agora.plus(expiracaoMinutos, ChronoUnit.MINUTES)))
				.signWith(chave)
				.compact();
	}

	public UsuarioAutenticado validarToken(String token) {
		Claims claims = Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload();
		Long id = ((Number) claims.get("id")).longValue();
		Perfil perfil = Perfil.valueOf(claims.get("perfil", String.class));
		return new UsuarioAutenticado(id, claims.getSubject(), perfil);
	}

	public long getExpiracaoMinutos() {
		return expiracaoMinutos;
	}
}
