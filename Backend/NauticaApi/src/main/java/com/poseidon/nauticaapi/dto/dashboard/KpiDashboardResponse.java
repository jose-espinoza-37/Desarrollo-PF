package com.poseidon.nauticaapi.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Indicadores del periodo: conteos, tasas y estadísticos del tiempo de atención. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiDashboardResponse {
    private Long totalSolicitudes;
    private Long pendientesVerificacion;
    private Long pendientesAprobacion;
    private Long sociosActivos;
    private Double tasaAprobacion;
    private Double tasaNoAptos;
    private Double promedioHorasAtencion;
    private Double medianaHorasAtencion;
    private Double desviacionHorasAtencion;
    private Double edadPromedio;
    private Long reincidentes;
    private Double correlacionEdadEmbarcaciones;
}
