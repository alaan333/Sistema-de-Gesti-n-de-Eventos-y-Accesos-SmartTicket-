package com.example.api.controller;

import com.example.api.dto.CotizacionRequest;
import com.example.api.dto.CotizacionResponse;
import com.example.api.service.CotizacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cotizar")
public class CotizacionController {

    private final CotizacionService cotizacionService;

    public CotizacionController(CotizacionService cotizacionService) {
        this.cotizacionService = cotizacionService;
    }

    @PostMapping
    public ResponseEntity<CotizacionResponse> cotizar(@RequestBody CotizacionRequest request) {
        return ResponseEntity.ok(cotizacionService.cotizar(request));
    }

}