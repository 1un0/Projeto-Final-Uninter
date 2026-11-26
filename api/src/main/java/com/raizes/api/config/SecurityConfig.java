package com.raizes.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.raizes.api.repository.UsuarioRepository;
import com.raizes.api.security.JwtAuthFilter;
import com.raizes.api.security.JwtService;

@Configuration
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public UserDetailsService userDetailsService(UsuarioRepository repository) {
		return email -> repository.findByEmail(email)
				.map(u -> User.withUsername(u.getEmail())
						.password(u.getSenha())
						.roles(u.getPerfil().name())
						.disabled(!Boolean.TRUE.equals(u.getContaAtiva()))
						.build())
				.orElseThrow(() -> new UsernameNotFoundException(email));
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/error", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/auth/login", "/auth/registro").permitAll()
						.requestMatchers(HttpMethod.GET, "/produtos/**", "/unidades/**").permitAll()

						.requestMatchers("/usuarios/**").hasRole("ADMIN")
						.requestMatchers("/produtos/**", "/unidades/**").hasAnyRole("ADMIN", "GERENTE")

						.requestMatchers(HttpMethod.GET, "/estoques/**").hasAnyRole("ADMIN", "GERENTE", "ATENDENTE")
						.requestMatchers("/estoques/**").hasAnyRole("ADMIN", "GERENTE")

						.requestMatchers(HttpMethod.POST, "/pedidos").authenticated()
						.requestMatchers(HttpMethod.GET, "/pedidos/meus").authenticated()
						.requestMatchers("/pedidos/**").hasAnyRole("ADMIN", "GERENTE", "ATENDENTE")

						.requestMatchers(HttpMethod.POST, "/pagamentos").authenticated()
						.requestMatchers("/pagamentos/**").hasAnyRole("ADMIN", "GERENTE", "ATENDENTE")
						.requestMatchers("/fidelidade/**").authenticated()
						.requestMatchers("/auditoria/**", "/newsletter/**").hasRole("ADMIN")

						.anyRequest().authenticated())
				.addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
