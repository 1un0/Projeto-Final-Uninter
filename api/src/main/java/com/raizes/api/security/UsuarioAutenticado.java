package com.raizes.api.security;

import com.raizes.api.model.enums.Perfil;

public record UsuarioAutenticado(Long id, String email, Perfil perfil) {
}
