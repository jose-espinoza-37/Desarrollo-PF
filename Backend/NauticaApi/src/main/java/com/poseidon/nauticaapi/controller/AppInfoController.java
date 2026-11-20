package com.poseidon.nauticaapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Ruta pública para comprobar que la API desplegada está en línea. */
@RestController
public class AppInfoController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> estado() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("aplicacion", "NauticaApi - Club Náutico Poseidón");
        info.put("estado", "activo");
        info.put("fecha", LocalDateTime.now());
        return ResponseEntity.ok(info);
    }
}
