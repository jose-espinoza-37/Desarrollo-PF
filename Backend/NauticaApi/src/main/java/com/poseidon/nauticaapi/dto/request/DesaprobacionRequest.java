package com.poseidon.nauticaapi.dto.request;

import com.poseidon.nauticaapi.validation.Patrones;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Motivo de la desaprobación (HU10). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DesaprobacionRequest {

    @NotBlank(message = "El motivo de la desaprobación es obligatorio.")
    @Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres.")
    @Pattern(regexp = Patrones.TEXTO, message = "El motivo contiene caracteres no permitidos.")
    private String motivo;
}
