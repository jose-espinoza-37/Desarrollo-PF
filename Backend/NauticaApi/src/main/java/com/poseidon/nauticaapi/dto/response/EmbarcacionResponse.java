package com.poseidon.nauticaapi.dto.response;

import com.poseidon.nauticaapi.entity.AsignacionRada;
import com.poseidon.nauticaapi.entity.Embarcacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Ficha de embarcación con sus documentos y la rada asignada. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbarcacionResponse {
    private Long id;
    private String nombre;
    private String matricula;
    private String tipo;
    private BigDecimal eslora;
    private BigDecimal manga;
    private BigDecimal calado;
    private Integer anioFabricacion;
    private String estado;
    private String observaciones;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaVerificacion;
    private String verificadoPor;
    private Long socioId;
    private String socioNombre;
    private String socioCorreo;
    private Long radaId;
    private String radaCodigo;
    private String radaMuelle;
    private BigDecimal radaTarifaMensual;
    private LocalDateTime fechaAsignacion;
    private List<DocumentoResponse> documentos;

    public static EmbarcacionResponse fromEntity(Embarcacion e, AsignacionRada asignacionActiva,
                                                 List<DocumentoResponse> documentos) {
        return EmbarcacionResponse.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .matricula(e.getMatricula())
                .tipo(e.getTipo())
                .eslora(e.getEslora())
                .manga(e.getManga())
                .calado(e.getCalado())
                .anioFabricacion(e.getAnioFabricacion())
                .estado(e.getEstado())
                .observaciones(e.getObservaciones())
                .fechaRegistro(e.getFechaRegistro())
                .fechaVerificacion(e.getFechaVerificacion())
                .verificadoPor(e.getVerificadoPor())
                .socioId(e.getSocio().getId())
                .socioNombre(e.getSocio().getNombres() + " " + e.getSocio().getApellidos())
                .socioCorreo(e.getSocio().getCorreo())
                .radaId(e.getRada() != null ? e.getRada().getId() : null)
                .radaCodigo(e.getRada() != null ? e.getRada().getCodigo() : null)
                .radaMuelle(e.getRada() != null ? e.getRada().getMuelle() : null)
                .radaTarifaMensual(e.getRada() != null ? e.getRada().getTarifaMensual() : null)
                .fechaAsignacion(asignacionActiva != null ? asignacionActiva.getFechaAsignacion() : null)
                .documentos(documentos)
                .build();
    }
}
