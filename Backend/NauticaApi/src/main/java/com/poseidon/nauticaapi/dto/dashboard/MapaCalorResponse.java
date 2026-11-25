package com.poseidon.nauticaapi.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Chart 3 (heatmap): solicitudes por día de la semana y franja horaria. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapaCalorResponse {
    private Integer diaSemana;
    private String dia;
    private String franja;
    private Long cantidad;
    private Double porcentaje;
}
