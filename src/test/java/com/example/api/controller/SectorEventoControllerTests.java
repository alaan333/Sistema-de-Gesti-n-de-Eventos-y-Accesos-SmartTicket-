package com.example.api.controller;

import com.example.api.entity.Entrada;
import com.example.api.entity.Sector;
import com.example.api.repository.EntradaRepository;
import com.example.api.repository.EventoRepository;
import com.example.api.repository.LugarRepository;
import com.example.api.repository.SectorRepository;
import java.math.BigDecimal;
import java.util.HashSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SectorEventoControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntradaRepository entradaRepository;

    @Autowired
    private LugarRepository lugarRepository;

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @BeforeEach
    void limpiarDatosAntesDeCadaPrueba() {
        limpiarDatos();
    }

    @AfterEach
    void limpiarDatosDespuesDeCadaPrueba() {
        limpiarDatos();
    }

    private void limpiarDatos() {
        entradaRepository.deleteAll();
        eventoRepository.deleteAll();
        sectorRepository.deleteAll();
        lugarRepository.deleteAll();
    }

    @Test
    void permiteCrearConsultarActualizarYEliminarSector() throws Exception {
        long lugarId = crearLugar();

        String location = mockMvc.perform(post("/api/sectores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Platea","capacidad":250,"precioBase":1500.0,"lugarId":%d}
                                """.formatted(lugarId)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/sectores/")))
                .andExpect(jsonPath("$.nombre").value("Platea"))
                .andExpect(jsonPath("$.lugar.id").value((int) lugarId))
                .andReturn()
                .getResponse()
                .getHeader("Location");
        long sectorId = idFromLocation(location);

        mockMvc.perform(get("/api/sectores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].capacidad").value(250));

        mockMvc.perform(get("/api/sectores/{id}", sectorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precioBase").value(1500.0));

        mockMvc.perform(put("/api/sectores/{id}", sectorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Platea VIP","capacidad":100,"precioBase":3000.0,"lugarId":%d}
                                """.formatted(lugarId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Platea VIP"));

        mockMvc.perform(delete("/api/sectores/{id}", sectorId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/sectores/{id}", sectorId))
                .andExpect(status().isNotFound());
    }

    @Test
    void permiteCrearConsultarActualizarYEliminarEvento() throws Exception {
        long lugarId = crearLugar();
        Sector platea = sectorRepository.save(new Sector(
                "Platea",
                2,
                new BigDecimal("12000.00"),
                lugarRepository.findById(lugarId).orElseThrow()
        ));
        Sector vip = sectorRepository.save(new Sector(
                "VIP",
                3,
                new BigDecimal("20000.00"),
                lugarRepository.findById(lugarId).orElseThrow()
        ));
        String request = """
                {"nombre":"Festival","descripcion":"Musica en vivo","fechaInicio":"2026-12-01T18:00:00","fechaFin":"2026-12-01T23:00:00","estado":"PROGRAMADO","lugarId":%d}
                """.formatted(lugarId);

        String location = mockMvc.perform(post("/api/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/eventos/")))
                .andExpect(jsonPath("$.nombre").value("Festival"))
                .andExpect(jsonPath("$.lugar.id").value((int) lugarId))
                .andReturn()
                .getResponse()
                .getHeader("Location");
        long eventoId = idFromLocation(location);

        var entradasGeneradas = entradaRepository.findAllByEvento_Id(eventoId);
        assertEquals(5, entradasGeneradas.size());
        assertEquals(2, entradasGeneradas.stream()
                .filter(entrada -> entrada.getSector().getId().equals(platea.getId()))
                .count());
        assertEquals(3, entradasGeneradas.stream()
                .filter(entrada -> entrada.getSector().getId().equals(vip.getId()))
                .count());
        assertTrue(
                entradasGeneradas.stream().allMatch(entrada -> "Disponible".equals(entrada.getEstado()))
        );
        assertTrue(
                entradasGeneradas.stream().allMatch(entrada ->
                        new BigDecimal(
                                entrada.getSector().getId().equals(platea.getId()) ? "12000.00" : "20000.00"
                        ).compareTo(entrada.getPrecio()) == 0)
        );
        assertEquals(
                entradasGeneradas.size(),
                new HashSet<>(entradasGeneradas.stream().map(Entrada::getCodigo).toList()).size()
        );

        mockMvc.perform(get("/api/eventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("PROGRAMADO"));

        mockMvc.perform(get("/api/eventos/{id}", eventoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Musica en vivo"));

        mockMvc.perform(put("/api/eventos/{id}", eventoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Festival Actualizado","descripcion":"Actualizado","fechaInicio":"2026-12-02T18:00:00","fechaFin":"2026-12-02T23:00:00","estado":"PUBLICADO","lugarId":%d}
                                """.formatted(lugarId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Festival Actualizado"));

        mockMvc.perform(delete("/api/eventos/{id}", eventoId))
                .andExpect(status().isNoContent());
        assertTrue(entradaRepository.findAllByEvento_Id(eventoId).isEmpty());

        mockMvc.perform(get("/api/eventos/{id}", eventoId))
                .andExpect(status().isNotFound());
    }

    @Test
    void rechazaRelacionesConLugaresInexistentesYExponeRecursosEnOpenApi() throws Exception {
        mockMvc.perform(post("/api/sectores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Platea","capacidad":100,"precioBase":50.0,"lugarId":99999}
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Festival","descripcion":"","fechaInicio":"2026-12-01T18:00:00","fechaFin":"2026-12-01T23:00:00","estado":"PROGRAMADO","lugarId":99999}
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/api/lugares")))
                .andExpect(content().string(containsString("/api/sectores")))
                .andExpect(content().string(containsString("/api/eventos")));
    }

    private long crearLugar() throws Exception {
        String location = mockMvc.perform(post("/api/lugares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Estadio","direccion":"Calle 123"}
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        return idFromLocation(location);
    }

    private long idFromLocation(String location) {
        return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    }
}
