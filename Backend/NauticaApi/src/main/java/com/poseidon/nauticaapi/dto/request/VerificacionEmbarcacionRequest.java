package com.poseidon.nauticaapi.dto.request;

import com.poseidon.nauticaapi.validation.Patrones;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Resultado de la verificación en los registros de Capitanías (HU12). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificacionEmbarcacionRequest {

    @NotBlank(message = "El resultado es obligatorio.")
    @Pattern(regexp = "^(VERIFICADA|OBSERVADA)$", message = "El resultado debe ser VERIFICADA u OBSERVADA.")
    private String resultado;

    @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres.")
    @Pattern(regexp = Patrones.TEXTO_OPCIONAL, message = "Las observaciones contienen caracteres no permitidos.")
    private String observaciones;
}
