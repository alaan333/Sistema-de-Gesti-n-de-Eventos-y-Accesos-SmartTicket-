package com.example.api.controller;

import com.example.api.repository.LugarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
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
class LugarControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LugarRepository lugarRepository;

    @BeforeEach
    void limpiarLugares() {
        lugarRepository.deleteAll();
    }

    @Test
    void permiteCrearListarActualizarYEliminarLugar() throws Exception {
        String location = mockMvc.perform(post("/api/lugares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Teatro Central","direccion":"Av. Principal 123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/lugares/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value("Teatro Central"))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        long id = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(get("/api/lugares"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Teatro Central"));

        mockMvc.perform(get("/api/lugares/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.direccion").value("Av. Principal 123"));

        mockMvc.perform(put("/api/lugares/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Teatro Nuevo","direccion":"Calle 456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Teatro Nuevo"));

        mockMvc.perform(delete("/api/lugares/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/lugares/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void rechazaCamposObligatoriosVaciosYDocumentaLaApi() throws Exception {
        mockMvc.perform(post("/api/lugares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":" ","direccion":" "}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/api/lugares")));
    }
}
