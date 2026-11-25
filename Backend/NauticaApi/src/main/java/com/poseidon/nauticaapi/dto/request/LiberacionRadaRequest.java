package com.poseidon.nauticaapi.dto.request;

import com.poseidon.nauticaapi.validation.Patrones;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Motivo para liberar una rada (queda en el historial). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiberacionRadaRequest {

    @NotBlank(message = "El motivo de la liberación es obligatorio.")
    @Size(min = 5, max = 300, message = "El motivo debe tener entre 5 y 300 caracteres.")
    @Pattern(regexp = Patrones.TEXTO, message = "El motivo contiene caracteres no permitidos.")
    private String motivo;
}
