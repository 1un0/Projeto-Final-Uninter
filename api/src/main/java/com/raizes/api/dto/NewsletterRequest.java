package com.raizes.api.dto;

import jakarta.validation.constraints.NotBlank;

public record NewsletterRequest(@NotBlank String assunto, @NotBlank String mensagem) {
}
