package com.poseidon.nauticaapi.dto.response;

import com.poseidon.nauticaapi.entity.SolicitudSocio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/** Constancia que recibe el postulante al registrar su solicitud (HU08). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudCreadaResponse {
    private String mensaje;
    private UUID codigo;
    private String estado;
    private LocalDateTime fechaSolicitud;

    public static SolicitudCreadaResponse fromEntity(SolicitudSocio s) {
        return SolicitudCreadaResponse.builder()
                .mensaje("Solicitud registrada correctamente.")
                .codigo(s.getCodigo())
                .estado(s.getEstado())
                .fechaSolicitud(s.getFechaSolicitud())
                .build();
    }
}
