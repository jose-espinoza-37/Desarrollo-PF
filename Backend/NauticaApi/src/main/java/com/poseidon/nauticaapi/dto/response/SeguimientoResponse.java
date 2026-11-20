package com.poseidon.nauticaapi.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;


public record SeguimientoResponse(
        UUID codigo,
        String nombres,
        String estado,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaResolucion,
        String motivoDesaprobacion) {
}
