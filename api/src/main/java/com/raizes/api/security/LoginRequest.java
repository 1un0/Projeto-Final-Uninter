package com.raizes.api.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank @Email String email, @NotBlank String senha) {
}
