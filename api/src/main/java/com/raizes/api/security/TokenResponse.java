package com.raizes.api.security;

import com.raizes.api.model.enums.Perfil;

public record TokenResponse(String token, String tipo, long expiraEmMinutos, Long id, String nome, Perfil perfil) {
}
