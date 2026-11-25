package com.poseidon.nauticaapi.dto.response;

import com.poseidon.nauticaapi.entity.AsignacionRada;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Fila del historial de asignaciones y liberaciones de radas. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionRadaResponse {
    private Long id;
    private String radaCodigo;
    private String embarcacionNombre;
    private String embarcacionMatricula;
    private String socioNombre;
    private LocalDateTime fechaAsignacion;
    private String asignadoPor;
    private LocalDateTime fechaLiberacion;
    private String liberadoPor;
    private String motivoLiberacion;
    private Boolean activa;

    public static AsignacionRadaResponse fromEntity(AsignacionRada a) {
        return AsignacionRadaResponse.builder()
                .id(a.getId())
                .radaCodigo(a.getRada().getCodigo())
                .embarcacionNombre(a.getEmbarcacion().getNombre())
                .embarcacionMatricula(a.getEmbarcacion().getMatricula())
                .socioNombre(a.getEmbarcacion().getSocio().getNombres() + " "
                        + a.getEmbarcacion().getSocio().getApellidos())
                .fechaAsignacion(a.getFechaAsignacion())
                .asignadoPor(a.getAsignadoPor())
                .fechaLiberacion(a.getFechaLiberacion())
                .liberadoPor(a.getLiberadoPor())
                .motivoLiberacion(a.getMotivoLiberacion())
                .activa(a.getActiva())
                .build();
    }
}
