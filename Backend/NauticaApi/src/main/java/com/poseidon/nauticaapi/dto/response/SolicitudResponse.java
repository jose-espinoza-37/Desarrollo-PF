package com.poseidon.nauticaapi.dto.response;

import com.poseidon.nauticaapi.entity.SolicitudSocio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SolicitudResponse(
        Long id,
        UUID codigo,
        String nombres,
        String apellidos,
        String dni,
        LocalDate fechaNacimiento,
        String correo,
        String celular,
        String direccion,
        boolean poseeEmbarcacion,
        int cantidadEmbarcaciones,
        String estado,
        String clasificacion,
        String resultadoVerificacion,
        String motivoDesaprobacion,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaVerificacion,
        LocalDateTime fechaResolucion,
        String verificadoPor,
        String resueltoPor,
        // Alerta HU09: hay otras solicitudes con el mismo DNI
        boolean dniDuplicado,
        long solicitudesConMismoDni) {

    public static SolicitudResponse desde(SolicitudSocio s, long solicitudesConMismoDni) {
        return new SolicitudResponse(
                s.getId(), s.getCodigo(), s.getNombres(), s.getApellidos(), s.getDni(),
                s.getFechaNacimiento(), s.getCorreo(), s.getCelular(), s.getDireccion(),
                s.isPoseeEmbarcacion(), s.getCantidadEmbarcaciones(), s.getEstado(), s.getClasificacion(),
                s.getResultadoVerificacion(), s.getMotivoDesaprobacion(), s.getFechaSolicitud(),
                s.getFechaVerificacion(), s.getFechaResolucion(), s.getVerificadoPor(), s.getResueltoPor(),
                solicitudesConMismoDni > 1, solicitudesConMismoDni);
    }
}
