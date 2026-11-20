package com.poseidon.nauticaapi.dto.response;

import com.poseidon.nauticaapi.entity.Modulo;


public record ModuloResponse(
        String codigo,
        String nombre,
        String ruta,
        String icono,
        Integer orden) {

    public static ModuloResponse desde(Modulo modulo) {
        return new ModuloResponse(
                modulo.getCodigo(),
                modulo.getNombre(),
                modulo.getRuta(),
                modulo.getIcono(),
                modulo.getOrden());
    }
}
