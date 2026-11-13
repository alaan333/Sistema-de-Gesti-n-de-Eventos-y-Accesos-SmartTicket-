package com.example.api.controller;

import com.example.api.dto.EventoRequest;
import com.example.api.entity.Evento;
import com.example.api.entity.Lugar;
import com.example.api.repository.EntradaRepository;
import com.example.api.repository.EventoRepository;
import com.example.api.repository.LugarRepository;
import com.example.api.service.EventoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoRepository eventoRepository;
    private final LugarRepository lugarRepository;
    private final EntradaRepository entradaRepository;
    private final EventoService eventoService;

    public EventoController(
            EventoRepository eventoRepository,
            LugarRepository lugarRepository,
            EntradaRepository entradaRepository,
            EventoService eventoService
    ) {
        this.eventoRepository = eventoRepository;
        this.lugarRepository = lugarRepository;
        this.entradaRepository = entradaRepository;
        this.eventoService = eventoService;
    }

    @PostMapping
    public ResponseEntity<Evento> crear(@Valid @RequestBody EventoRequest request) {
        Optional<Evento> evento = eventoService.crear(request);
        if (evento.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(evento.get().getId())
                .toUri();

        return ResponseEntity.created(location).body(evento.get());
    }

    @GetMapping
    public List<Evento> listar() {
        return eventoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Evento> buscarPorId(@PathVariable Long id) {
        return eventoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Evento> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EventoRequest request
    ) {
        Optional<Evento> evento = eventoRepository.findById(id);
        if (evento.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<Lugar> lugar = lugarRepository.findById(request.lugarId());
        if (lugar.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        actualizarDatos(evento.get(), request, lugar.get());
        return ResponseEntity.ok(eventoRepository.save(evento.get()));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!eventoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        entradaRepository.deleteAllByEvento_Id(id);
        eventoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void actualizarDatos(Evento evento, EventoRequest request, Lugar lugar) {
        evento.setNombre(request.nombre());
        evento.setDescripcion(request.descripcion());
        evento.setFechaInicio(request.fechaInicio());
        evento.setFechaFin(request.fechaFin());
        evento.setEstado(request.estado());
        evento.setLugar(lugar);
    }
}
