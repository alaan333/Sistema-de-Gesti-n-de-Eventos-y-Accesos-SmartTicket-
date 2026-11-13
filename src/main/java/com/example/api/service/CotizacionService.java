package com.example.api.service;

import com.example.api.dto.CotizacionRequest;
import com.example.api.dto.CotizacionResponse;
import com.example.api.entity.Sector;
import com.example.api.exception.CotizacionException;
import com.example.api.repository.EntradaRepository;
import com.example.api.repository.EventoRepository;
import com.example.api.repository.SectorRepository;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CotizacionService {

    private static final String ESTADO_DISPONIBLE = "Disponible";

    private final EntradaRepository entradaRepository;
    private final EventoRepository eventoRepository;
    private final SectorRepository sectorRepository;

    public CotizacionService(
            EntradaRepository entradaRepository,
            EventoRepository eventoRepository,
            SectorRepository sectorRepository
    ) {
        this.entradaRepository = entradaRepository;
        this.eventoRepository = eventoRepository;
        this.sectorRepository = sectorRepository;
    }

    @Transactional(readOnly = true)
    public CotizacionResponse cotizar(CotizacionRequest request) {
        if (request == null || request.cantidad() == null || request.cantidad() <= 0) {
            throw new CotizacionException(HttpStatus.BAD_REQUEST, "Cantidad inválida");
        }

        if (request.eventoId() == null || !eventoRepository.existsById(request.eventoId())) {
            throw new CotizacionException(HttpStatus.NOT_FOUND, "Evento no encontrado");
        }

        Sector sector = request.sectorId() == null
                ? null
                : sectorRepository.findById(request.sectorId()).orElse(null);
        if (sector == null) {
            throw new CotizacionException(HttpStatus.NOT_FOUND, "Sector no encontrado");
        }

        long disponibles = entradaRepository.countByEvento_IdAndSector_IdAndEstadoIgnoreCase(
                request.eventoId(),
                request.sectorId(),
                ESTADO_DISPONIBLE
        );
        if (disponibles < request.cantidad()) {
            throw new CotizacionException(HttpStatus.BAD_REQUEST, "Capacidad insuficiente");
        }

        if (sector.getPrecioBase() == null || sector.getPrecioBase().signum() < 0) {
            throw new CotizacionException(HttpStatus.BAD_REQUEST, "Precio del sector inválido");
        }

        BigDecimal subtotal = sector.getPrecioBase().multiply(BigDecimal.valueOf(request.cantidad()));
        return new CotizacionResponse(
                request.eventoId(),
                request.sectorId(),
                request.cantidad(),
                sector.getPrecioBase(),
                subtotal,
                subtotal
        );
    }
}
