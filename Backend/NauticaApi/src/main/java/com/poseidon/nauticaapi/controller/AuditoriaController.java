package com.poseidon.nauticaapi.controller;

import com.poseidon.nauticaapi.dto.response.EventoResponse;
import com.poseidon.nauticaapi.service.AuditoriaConsultaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Bitácora de auditoría (solo Gerente). */
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    @Autowired
    private AuditoriaConsultaService auditoriaConsultaService;

    @GetMapping("/eventos")
    public ResponseEntity<List<EventoResponse>> eventos() {
        return ResponseEntity.ok(auditoriaConsultaService.listarEventos());
    }
}
