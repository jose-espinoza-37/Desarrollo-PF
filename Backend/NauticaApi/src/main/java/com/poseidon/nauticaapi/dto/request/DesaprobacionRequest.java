package com.poseidon.nauticaapi.dto.request;

import com.poseidon.nauticaapi.validation.Patrones;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record DesaprobacionRequest(

        @NotBlank(message = "El motivo de la desaprobación es obligatorio.")
        @Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres.")
        @Pattern(regexp = Patrones.TEXTO, message = "El motivo contiene caracteres no permitidos.")
        String motivo) {
}
