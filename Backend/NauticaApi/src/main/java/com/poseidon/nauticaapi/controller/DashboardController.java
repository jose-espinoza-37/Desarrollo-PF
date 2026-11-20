package com.poseidon.nauticaapi.controller;

import com.poseidon.nauticaapi.dto.response.DashboardResponse;
import com.poseidon.nauticaapi.service.DashboardService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }


    @GetMapping("/resumen")
    public ResponseEntity<DashboardResponse> resumen() {
        return ResponseEntity.ok(dashboardService.resumen());
    }
}
