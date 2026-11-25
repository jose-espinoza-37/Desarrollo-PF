package com.poseidon.nauticaapi.dto.response;

import com.poseidon.nauticaapi.entity.Modulo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Opción del menú que se envía al frontend. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuloResponse {
    private String codigo;
    private String nombre;
    private String ruta;
    private String icono;
    private Integer orden;

    public static ModuloResponse fromEntity(Modulo modulo) {
        return ModuloResponse.builder()
                .codigo(modulo.getCodigo())
                .nombre(modulo.getNombre())
                .ruta(modulo.getRuta())
                .icono(modulo.getIcono())
                .orden(modulo.getOrden())
                .build();
    }

    public static List<ModuloResponse> fromEntities(List<Modulo> modulos) {
        return modulos.stream().map(ModuloResponse::fromEntity).toList();
    }
}
