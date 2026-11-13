package com.example.api.service;

import com.example.api.dto.EventoRequest;
import com.example.api.entity.Entrada;
import com.example.api.entity.Evento;
import com.example.api.entity.Lugar;
import com.example.api.entity.Sector;
import com.example.api.repository.EntradaRepository;
import com.example.api.repository.EventoRepository;
import com.example.api.repository.LugarRepository;
import com.example.api.repository.SectorRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventoService {

    private static final String ESTADO_ENTRADA_DISPONIBLE = "Disponible";

    private final EventoRepository eventoRepository;
    private final LugarRepository lugarRepository;
    private final SectorRepository sectorRepository;
    private final EntradaRepository entradaRepository;

    public EventoService(
            EventoRepository eventoRepository,
            LugarRepository lugarRepository,
            SectorRepository sectorRepository,
            EntradaRepository entradaRepository
    ) {
        this.eventoRepository = eventoRepository;
        this.lugarRepository = lugarRepository;
        this.sectorRepository = sectorRepository;
        this.entradaRepository = entradaRepository;
    }

    @Transactional
    public Optional<Evento> crear(EventoRequest request) {
        Optional<Lugar> lugar = lugarRepository.findById(request.lugarId());
        if (lugar.isEmpty()) {
            return Optional.empty();
        }

        List<Sector> sectores = sectorRepository.findAllByLugar_Id(lugar.get().getId());
        Evento evento = eventoRepository.save(new Evento(
                request.nombre(),
                request.descripcion(),
                request.fechaInicio(),
                request.fechaFin(),
                request.estado(),
                lugar.get()
        ));

        List<Entrada> entradas = sectores.stream()
                .flatMap(sector -> crearEntradas(evento, sector).stream())
                .toList();
        entradaRepository.saveAll(entradas);

        return Optional.of(evento);
    }

    private List<Entrada> crearEntradas(Evento evento, Sector sector) {
        if (sector.getCapacidad() == null || sector.getCapacidad() < 0) {
            throw new IllegalStateException(
                    "La capacidad del sector " + sector.getId() + " debe ser mayor o igual a cero"
            );
        }
        if (sector.getPrecioBase() == null || sector.getPrecioBase().signum() < 0) {
            throw new IllegalStateException(
                    "El precio del sector " + sector.getId() + " debe ser mayor o igual a cero"
            );
        }

        return java.util.stream.IntStream.range(0, sector.getCapacidad())
                .mapToObj(index -> new Entrada(
                        UUID.randomUUID().toString(),
                        sector.getNombre(),
                        sector.getPrecioBase(),
                        ESTADO_ENTRADA_DISPONIBLE,
                        evento,
                        sector
                ))
                .toList();
    }
}
