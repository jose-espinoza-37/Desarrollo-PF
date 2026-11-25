package com.poseidon.nauticaapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Datos del documento sin el archivo (el archivo se descarga aparte). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoResponse {
    private Long id;
    private String tipoDocumento;
    private String nombreArchivo;
    private String contentType;
    private Long tamano;
    private LocalDateTime fechaCarga;
    private Long embarcacionId;
}
