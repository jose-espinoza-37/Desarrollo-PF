package com.poseidon.nauticaapi.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Chart 4 (radar): distribución de la clasificación de pago por rango de edad. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfilEdadResponse {
    private String rango;
    private String clasificacion;
    private String descripcion;
    private Long cantidad;
    private Double porcentaje;
}
