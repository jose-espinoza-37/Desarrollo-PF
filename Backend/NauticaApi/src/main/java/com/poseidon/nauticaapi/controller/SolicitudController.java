package com.poseidon.nauticaapi.controller;

import com.poseidon.nauticaapi.dto.request.DesaprobacionRequest;
import com.poseidon.nauticaapi.dto.request.SolicitudRequest;
import com.poseidon.nauticaapi.dto.request.VerificacionRequest;
import com.poseidon.nauticaapi.dto.response.SeguimientoResponse;
import com.poseidon.nauticaapi.dto.response.SolicitudCreadaResponse;
import com.poseidon.nauticaapi.dto.response.SolicitudResponse;
import com.poseidon.nauticaapi.service.SolicitudService;
import com.poseidon.nauticaapi.exception.RecursoNoEncontradoException;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    @Autowired
    private SolicitudService solicitudService;


    @PostMapping
    public ResponseEntity<SolicitudCreadaResponse> registrar(@Valid @RequestBody SolicitudRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitudService.registrar(request));
    }


    @GetMapping("/seguimiento/{codigo}")
    public ResponseEntity<SeguimientoResponse> seguimiento(@PathVariable("codigo") String codigo) {
        UUID uuid;
        try {
            uuid = UUID.fromString(codigo.trim());
        } catch (IllegalArgumentException e) {
            throw new RecursoNoEncontradoException("El código de seguimiento no tiene un formato válido.");
        }
        return ResponseEntity.ok(solicitudService.seguimiento(uuid));
    }

    @GetMapping
    public ResponseEntity<List<SolicitudResponse>> listar(@RequestParam(name = "estado", required = false) String estado) {
        return ResponseEntity.ok(solicitudService.listar(estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponse> obtener(@PathVariable("id") Long id) {
        return ResponseEntity.ok(solicitudService.obtener(id));
    }


    @PutMapping("/{id}/verificar")
    public ResponseEntity<SolicitudResponse> verificar(
            @PathVariable("id") Long id,
            @Valid @RequestBody VerificacionRequest request,
            Authentication autenticacion) {

        return ResponseEntity.ok(solicitudService.verificar(id, request, autenticacion.getName()));
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<SolicitudResponse> aprobar(@PathVariable("id") Long id, Authentication autenticacion) {
        return ResponseEntity.ok(solicitudService.aprobar(id, autenticacion.getName()));
    }


    @PutMapping("/{id}/desaprobar")
    public ResponseEntity<SolicitudResponse> desaprobar(
            @PathVariable("id") Long id,
            @Valid @RequestBody DesaprobacionRequest request,
            Authentication autenticacion) {

        return ResponseEntity.ok(solicitudService.desaprobar(id, request.getMotivo(), autenticacion.getName()));
    }
}
