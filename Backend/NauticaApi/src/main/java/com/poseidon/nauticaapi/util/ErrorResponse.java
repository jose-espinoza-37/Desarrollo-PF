package com.poseidon.nauticaapi.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/** Cuerpo de todas las respuestas de error de la API. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    @Builder.Default
    private String fecha = LocalDateTime.now().toString();
    private Integer estado;
    private String error;
    private String mensaje;
    /** Errores de validación por campo (solo en 400). */
    private Map<String, String> mensajes;
}
