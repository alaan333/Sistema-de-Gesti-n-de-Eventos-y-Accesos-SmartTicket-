package com.example.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record SectorRequest(
        @NotBlank String nombre,
        @NotNull @Positive Integer capacidad,
        @NotNull @Positive BigDecimal precioBase,
        @NotNull @Positive Long lugarId
) {
}
