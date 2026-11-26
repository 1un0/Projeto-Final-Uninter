package com.raizes.api.dto;

import jakarta.validation.constraints.NotNull;

public record PagamentoRequest(@NotNull Long idPedido, Boolean simularRecusa) {
}
