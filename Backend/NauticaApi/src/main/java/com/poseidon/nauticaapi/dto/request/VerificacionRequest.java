package com.poseidon.nauticaapi.dto.request;

import com.poseidon.nauticaapi.validation.Patrones;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerificacionRequest(

        @NotBlank(message = "La clasificación es obligatoria.")
        @Pattern(regexp = Patrones.CLASIFICACION,
                message = "Clasificación no válida (PAGADOR, PAGADOR_ESPORADICO o RENUENTE).")
        String clasificacion,

        @NotBlank(message = "Registra el resultado de la verificación con otros clubes.")
        @Size(min = 10, max = 500, message = "El resultado debe tener entre 10 y 500 caracteres.")
        @Pattern(regexp = Patrones.TEXTO, message = "El resultado contiene caracteres no permitidos.")
        String resultadoVerificacion) {
}
