package com.example.api.dto;

import java.math.BigDecimal;

public record CotizacionResponse(
        Long eventoId,
        Long sectorId,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        BigDecimal total
) {
}
