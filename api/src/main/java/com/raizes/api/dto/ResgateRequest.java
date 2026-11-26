package com.raizes.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ResgateRequest(@NotNull Long idPedido, @NotNull @Min(10) Integer pontos) {
}
