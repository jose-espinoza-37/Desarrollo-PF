package com.poseidon.nauticaapi.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Embarcación verificada a la que se asigna la rada (HU13). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionRadaRequest {

    @NotNull(message = "Selecciona la embarcación.")
    @Positive(message = "Embarcación no válida.")
    private Long embarcacionId;
}
