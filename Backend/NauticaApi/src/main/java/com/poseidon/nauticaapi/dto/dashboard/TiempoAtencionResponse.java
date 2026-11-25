package com.poseidon.nauticaapi.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Chart 2 (box plot): cuartiles del tiempo de atención en horas por clasificación. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TiempoAtencionResponse {
    private String clasificacion;
    private String descripcion;
    private Long cantidad;
    private Double minimo;
    private Double q1;
    private Double mediana;
    private Double q3;
    private Double maximo;
    private Double promedio;
    private Double desviacion;
}
