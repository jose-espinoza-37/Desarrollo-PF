package com.poseidon.nauticaapi.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Respuesta genérica: éxito, mensaje y el objeto creado. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectResponse<T> {
    private Boolean success;
    private String message;
    private T object;
}
