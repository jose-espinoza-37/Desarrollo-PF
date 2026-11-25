package com.poseidon.nauticaapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Cantidad de solicitudes por DNI (alerta de doble inscripción). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConteoDniResponse {
    private String dni;
    private Long cantidad;
}
