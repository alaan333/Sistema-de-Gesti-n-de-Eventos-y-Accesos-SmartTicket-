package com.example.api.controller;

import com.example.api.dto.SectorRequest;
import com.example.api.entity.Lugar;
import com.example.api.entity.Sector;
import com.example.api.repository.LugarRepository;
import com.example.api.repository.SectorRepository;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;
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
@RequestMapping("/api/sectores")
public class SectorController {

    private final SectorRepository sectorRepository;
    private final LugarRepository lugarRepository;

    public SectorController(SectorRepository sectorRepository, LugarRepository lugarRepository) {
        this.sectorRepository = sectorRepository;
        this.lugarRepository = lugarRepository;
    }

    @PostMapping
    public ResponseEntity<Sector> crear(@Valid @RequestBody SectorRequest request) {
        Optional<Lugar> lugar = lugarRepository.findById(request.lugarId());
        if (lugar.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Sector sector = sectorRepository.save(new Sector(
                request.nombre(),
                request.capacidad(),
                request.precioBase(),
                lugar.get()
        ));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(sector.getId())
                .toUri();

        return ResponseEntity.created(location).body(sector);
    }

    @GetMapping
    public List<Sector> listar() {
        return sectorRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sector> buscarPorId(@PathVariable Long id) {
        return sectorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sector> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SectorRequest request
    ) {
        Optional<Sector> sector = sectorRepository.findById(id);
        if (sector.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<Lugar> lugar = lugarRepository.findById(request.lugarId());
        if (lugar.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        actualizarDatos(sector.get(), request, lugar.get());
        return ResponseEntity.ok(sectorRepository.save(sector.get()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!sectorRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        sectorRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void actualizarDatos(Sector sector, SectorRequest request, Lugar lugar) {
        sector.setNombre(request.nombre());
        sector.setCapacidad(request.capacidad());
        sector.setPrecioBase(request.precioBase());
        sector.setLugar(lugar);
    }
}
