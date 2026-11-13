package com.example.api.repository;

import com.example.api.entity.Sector;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectorRepository extends JpaRepository<Sector, Long> {

    List<Sector> findAllByLugar_Id(Long lugarId);
}
