package com.poseidon.nauticaapi.controller;

import com.poseidon.nauticaapi.dto.request.AsignacionRadaRequest;
import com.poseidon.nauticaapi.dto.request.LiberacionRadaRequest;
import com.poseidon.nauticaapi.dto.response.AsignacionRadaResponse;
import com.poseidon.nauticaapi.dto.response.EmbarcacionResponse;
import com.poseidon.nauticaapi.dto.response.RadaCompatibleResponse;
import com.poseidon.nauticaapi.dto.response.RadaResponse;
import com.poseidon.nauticaapi.service.RadaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** HU13: radas (solo Servicios Navieros y Gerencia). */
@RestController
@RequestMapping("/api/radas")
public class RadaController {

    @Autowired
    private RadaService radaService;

    @GetMapping
    public ResponseEntity<List<RadaResponse>> listar() {
        return ResponseEntity.ok(radaService.listar());
    }

    @GetMapping("/por-asignar")
    public ResponseEntity<List<EmbarcacionResponse>> porAsignar() {
        return ResponseEntity.ok(radaService.porAsignar());
    }

    @GetMapping("/compatibles/{embarcacionId}")
    public ResponseEntity<List<RadaCompatibleResponse>> compatibles(@PathVariable("embarcacionId") Long embarcacionId) {
        return ResponseEntity.ok(radaService.compatibles(embarcacionId));
    }

    @PostMapping("/{radaId}/asignar")
    public ResponseEntity<RadaResponse> asignar(@PathVariable("radaId") Long radaId,
                                                @Valid @RequestBody AsignacionRadaRequest request,
                                                Authentication autenticacion) {
        return ResponseEntity.ok(radaService.asignar(radaId, request.getEmbarcacionId(), autenticacion.getName()));
    }

    @PutMapping("/{radaId}/liberar")
    public ResponseEntity<RadaResponse> liberar(@PathVariable("radaId") Long radaId,
                                                @Valid @RequestBody LiberacionRadaRequest request,
                                                Authentication autenticacion) {
        return ResponseEntity.ok(radaService.liberar(radaId, request.getMotivo(), autenticacion.getName()));
    }

    @GetMapping("/historial")
    public ResponseEntity<List<AsignacionRadaResponse>> historial() {
        return ResponseEntity.ok(radaService.historial());
    }
}
