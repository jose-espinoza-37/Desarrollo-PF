package com.poseidon.nauticaapi.dto.response;

import com.poseidon.nauticaapi.entity.SolicitudSocio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Solicitud tal como se muestra en las bandejas (HU09, HU10). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudResponse {
    private Long id;
    private UUID codigo;
    private String nombres;
    private String apellidos;
    private String dni;
    private LocalDate fechaNacimiento;
    private String correo;
    private String celular;
    private String direccion;
    private Boolean poseeEmbarcacion;
    private Integer cantidadEmbarcaciones;
    private String estado;
    private String clasificacion;
    private String resultadoVerificacion;
    private String motivoDesaprobacion;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaVerificacion;
    private LocalDateTime fechaResolucion;
    private String verificadoPor;
    private String resueltoPor;
    /** Alerta de doble inscripción: hay otras solicitudes con el mismo DNI. */
    private Boolean dniDuplicado;
    private Long solicitudesConMismoDni;

    public static SolicitudResponse fromEntity(SolicitudSocio s, Long solicitudesConMismoDni) {
        return SolicitudResponse.builder()
                .id(s.getId())
                .codigo(s.getCodigo())
                .nombres(s.getNombres())
                .apellidos(s.getApellidos())
                .dni(s.getDni())
                .fechaNacimiento(s.getFechaNacimiento())
                .correo(s.getCorreo())
                .celular(s.getCelular())
                .direccion(s.getDireccion())
                .poseeEmbarcacion(s.getPoseeEmbarcacion())
                .cantidadEmbarcaciones(s.getCantidadEmbarcaciones())
                .estado(s.getEstado())
                .clasificacion(s.getClasificacion())
                .resultadoVerificacion(s.getResultadoVerificacion())
                .motivoDesaprobacion(s.getMotivoDesaprobacion())
                .fechaSolicitud(s.getFechaSolicitud())
                .fechaVerificacion(s.getFechaVerificacion())
                .fechaResolucion(s.getFechaResolucion())
                .verificadoPor(s.getVerificadoPor())
                .resueltoPor(s.getResueltoPor())
                .dniDuplicado(solicitudesConMismoDni > 1)
                .solicitudesConMismoDni(solicitudesConMismoDni)
                .build();
    }
}
