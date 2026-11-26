package com.raizes.api.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	public JwtAuthFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer ")) {
			try {
				UsuarioAutenticado usuario = jwtService.validarToken(header.substring(7));
				var autenticacao = new UsernamePasswordAuthenticationToken(usuario, null,
						List.of(new SimpleGrantedAuthority("ROLE_" + usuario.perfil().name())));
				SecurityContextHolder.getContext().setAuthentication(autenticacao);
			} catch (JwtException | IllegalArgumentException e) {
				SecurityContextHolder.clearContext();
			}
		}
		chain.doFilter(request, response);
	}
}
