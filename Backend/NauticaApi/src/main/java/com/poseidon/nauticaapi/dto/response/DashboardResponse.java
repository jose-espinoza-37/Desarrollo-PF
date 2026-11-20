package com.poseidon.nauticaapi.dto.response;

import java.util.List;


public record DashboardResponse(
        long totalSolicitudes,
        long pendientesVerificacion,
        long pendientesAprobacion,
        long sociosActivos,
        double tasaAprobacion,
        List<SerieDia> solicitudesPorDia,     // Chart 1: área
        List<Conteo> porClasificacion,       // Chart 2: radialBar
        List<Conteo> porEstado) {            // Chart 3: treemap

    public record SerieDia(String fecha, long cantidad) {
    }

    public record Conteo(String codigo, String etiqueta, long cantidad, double porcentaje) {
    }
}
