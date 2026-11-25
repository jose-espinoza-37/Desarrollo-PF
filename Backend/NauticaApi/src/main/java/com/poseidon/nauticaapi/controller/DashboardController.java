package com.poseidon.nauticaapi.controller;

import com.poseidon.nauticaapi.dto.dashboard.KpiDashboardResponse;
import com.poseidon.nauticaapi.dto.dashboard.MapaCalorResponse;
import com.poseidon.nauticaapi.dto.dashboard.PerfilEdadResponse;
import com.poseidon.nauticaapi.dto.dashboard.SerieTemporalResponse;
import com.poseidon.nauticaapi.dto.dashboard.TiempoAtencionResponse;
import com.poseidon.nauticaapi.service.DashboardService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Panel gerencial. Todos los endpoints reciben el periodo de análisis en días (7 a 365). */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/kpis")
    public ResponseEntity<KpiDashboardResponse> kpis(
            @RequestParam(name = "dias", defaultValue = "90") @Min(7) @Max(365) Integer dias) {
        return ResponseEntity.ok(dashboardService.getKpis(dias));
    }

    @GetMapping("/serie-temporal")
    public ResponseEntity<List<SerieTemporalResponse>> serieTemporal(
            @RequestParam(name = "dias", defaultValue = "90") @Min(7) @Max(365) Integer dias) {
        return ResponseEntity.ok(dashboardService.getSerieTemporal(dias));
    }

    @GetMapping("/tiempos-atencion")
    public ResponseEntity<List<TiempoAtencionResponse>> tiemposAtencion(
            @RequestParam(name = "dias", defaultValue = "90") @Min(7) @Max(365) Integer dias) {
        return ResponseEntity.ok(dashboardService.getTiemposAtencion(dias));
    }

    @GetMapping("/mapa-calor")
    public ResponseEntity<List<MapaCalorResponse>> mapaCalor(
            @RequestParam(name = "dias", defaultValue = "90") @Min(7) @Max(365) Integer dias) {
        return ResponseEntity.ok(dashboardService.getMapaCalor(dias));
    }

    @GetMapping("/perfil-edad")
    public ResponseEntity<List<PerfilEdadResponse>> perfilEdad(
            @RequestParam(name = "dias", defaultValue = "90") @Min(7) @Max(365) Integer dias) {
        return ResponseEntity.ok(dashboardService.getPerfilEdad(dias));
    }
}
