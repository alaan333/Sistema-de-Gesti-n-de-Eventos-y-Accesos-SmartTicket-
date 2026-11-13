package com.example.api.controller;

import com.example.api.entity.Entrada;
import com.example.api.entity.Evento;
import com.example.api.entity.Lugar;
import com.example.api.entity.Sector;
import com.example.api.repository.EntradaRepository;
import com.example.api.repository.EventoRepository;
import com.example.api.repository.LugarRepository;
import com.example.api.repository.SectorRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CotizacionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntradaRepository entradaRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private LugarRepository lugarRepository;

    private Long eventoId;
    private Long sectorId;

    @BeforeEach
    void prepararDatos() {
        limpiarDatos();

        Lugar lugar = lugarRepository.save(new Lugar("Estadio", "Av. Principal 100"));
        Sector sector = sectorRepository.save(new Sector(
                "Platea",
                100,
                new BigDecimal("12000.00"),
                lugar
        ));
        Evento evento = eventoRepository.save(new Evento(
                "Rock Fest",
                "Festival de rock",
                LocalDateTime.of(2026, 12, 1, 18, 0),
                LocalDateTime.of(2026, 12, 1, 23, 0),
                "PROGRAMADO",
                lugar
        ));

        eventoId = evento.getId();
        sectorId = sector.getId();
    }

    @AfterEach
    void limpiarDespuesDePrueba() {
        limpiarDatos();
    }

    @Test
    void cotizaSubtotalExactoSiHayDisponibilidad() throws Exception {
        guardarEntrada("DISPONIBLE");
        guardarEntrada("Disponible");

        mockMvc.perform(post("/api/cotizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventoId":%d,"sectorId":%d,"cantidad":2}
                                """.formatted(eventoId, sectorId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precioUnitario").value(12000.0))
                .andExpect(jsonPath("$.subtotal").value(24000.0))
                .andExpect(jsonPath("$.total").value(24000.0));
    }

    @Test
    void rechazaCotizacionCuandoNoHaySuficientesEntradasDisponibles() throws Exception {
        guardarEntrada("Disponible");
        guardarEntrada("Vendida");

        mockMvc.perform(post("/api/cotizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventoId":%d,"sectorId":%d,"cantidad":3}
                                """.formatted(eventoId, sectorId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Capacidad insuficiente"));
    }

    @Test
    void rechazaCantidadCeroONegativaAntesDeConsultarDisponibilidad() throws Exception {
        guardarEntrada("Disponible");

        mockMvc.perform(post("/api/cotizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventoId":%d,"sectorId":%d,"cantidad":0}
                                """.formatted(eventoId, sectorId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cantidad inválida"));

        mockMvc.perform(post("/api/cotizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventoId":%d,"sectorId":%d,"cantidad":-1}
                                """.formatted(eventoId, sectorId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cantidad inválida"));
    }

    private void guardarEntrada(String estado) {
        entradaRepository.save(new Entrada(
                "TICKET-" + System.nanoTime(),
                "GENERAL",
                new BigDecimal("12000.00"),
                estado,
                eventoRepository.findById(eventoId).orElseThrow(),
                sectorRepository.findById(sectorId).orElseThrow()
        ));
    }

    private void limpiarDatos() {
        entradaRepository.deleteAll();
        eventoRepository.deleteAll();
        sectorRepository.deleteAll();
        lugarRepository.deleteAll();
    }
}
