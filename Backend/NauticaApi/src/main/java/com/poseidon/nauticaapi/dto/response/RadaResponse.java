package com.poseidon.nauticaapi.dto.response;

import com.poseidon.nauticaapi.entity.Embarcacion;
import com.poseidon.nauticaapi.entity.Rada;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Rada con la embarcación que la ocupa (si está ocupada). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RadaResponse {
    private Long id;
    private String codigo;
    private String muelle;
    private BigDecimal esloraMax;
    private BigDecimal mangaMax;
    private BigDecimal caladoMax;
    private BigDecimal tarifaMensual;
    private String estado;
    private Long embarcacionId;
    private String embarcacionNombre;
    private String embarcacionMatricula;
    private String socioNombre;

    public static RadaResponse fromEntity(Rada r, Embarcacion ocupante) {
        return RadaResponse.builder()
                .id(r.getId())
                .codigo(r.getCodigo())
                .muelle(r.getMuelle())
                .esloraMax(r.getEsloraMax())
                .mangaMax(r.getMangaMax())
                .caladoMax(r.getCaladoMax())
                .tarifaMensual(r.getTarifaMensual())
                .estado(r.getEstado())
                .embarcacionId(ocupante != null ? ocupante.getId() : null)
                .embarcacionNombre(ocupante != null ? ocupante.getNombre() : null)
                .embarcacionMatricula(ocupante != null ? ocupante.getMatricula() : null)
                .socioNombre(ocupante != null
                        ? ocupante.getSocio().getNombres() + " " + ocupante.getSocio().getApellidos() : null)
                .build();
    }
}
