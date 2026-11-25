package com.poseidon.nauticaapi.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Chart 1: solicitudes por día con media móvil de 7 días y acumulado. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SerieTemporalResponse {
    private String fecha;
    private Long cantidad;
    private Long aprobadas;
    private Double mediaMovil;
    private Long acumulado;
}
