package com.poseidon.nauticaapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Estado de la API en la ruta raíz "/". */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppInfoResponse {
    private String aplicacion;
    private String descripcion;
    private String version;
    private String estado;
}
