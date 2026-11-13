package com.example.api.dto;

import jakarta.validation.constraints.NotBlank;

public record LugarRequest(
        @NotBlank String nombre,
        @NotBlank String direccion
) {
}
