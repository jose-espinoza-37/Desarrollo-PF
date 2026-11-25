package com.poseidon.nauticaapi.dto.response;

import com.poseidon.nauticaapi.entity.SolicitudSocio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/** Lo que ve el postulante al consultar con su código (sin DNI ni correo). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeguimientoResponse {
    private UUID codigo;
    private String nombres;
    private String estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaResolucion;
    private String motivoDesaprobacion;

    public static SeguimientoResponse fromEntity(SolicitudSocio s) {
        return SeguimientoResponse.builder()
                .codigo(s.getCodigo())
                .nombres(s.getNombres())
                .estado(s.getEstado())
                .fechaSolicitud(s.getFechaSolicitud())
                .fechaResolucion(s.getFechaResolucion())
                .motivoDesaprobacion(s.getMotivoDesaprobacion())
                .build();
    }
}
