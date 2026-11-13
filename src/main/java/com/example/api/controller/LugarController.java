package com.example.api.controller;

import com.example.api.dto.LugarRequest;
import com.example.api.entity.Lugar;
import com.example.api.repository.LugarRepository;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
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
@RequestMapping("/api/lugares")
public class LugarController {

    private final LugarRepository lugarRepository;

    public LugarController(LugarRepository lugarRepository) {
        this.lugarRepository = lugarRepository;
    }

    @PostMapping
    public ResponseEntity<Lugar> crear(@Valid @RequestBody LugarRequest request) {
        Lugar lugar = lugarRepository.save(new Lugar(request.nombre(), request.direccion()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(lugar.getId())
                .toUri();

        return ResponseEntity.created(location).body(lugar);
    }

    @GetMapping
    public List<Lugar> listar() {
        return lugarRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lugar> buscarPorId(@PathVariable Long id) {
        return lugarRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Lugar> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody LugarRequest request
    ) {
        return lugarRepository.findById(id)
                .map(lugar -> {
                    lugar.setNombre(request.nombre());
                    lugar.setDireccion(request.direccion());
                    return ResponseEntity.ok(lugarRepository.save(lugar));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!lugarRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        lugarRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
