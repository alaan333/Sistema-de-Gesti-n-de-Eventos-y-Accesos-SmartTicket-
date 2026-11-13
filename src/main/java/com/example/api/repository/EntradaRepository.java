package com.example.api.repository;

import com.example.api.entity.Entrada;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntradaRepository extends JpaRepository<Entrada, Long> {

    long countByEvento_IdAndSector_IdAndEstadoIgnoreCase(Long eventoId, Long sectorId, String estado);

    List<Entrada> findAllByEvento_Id(Long eventoId);

    void deleteAllByEvento_Id(Long eventoId);
}
