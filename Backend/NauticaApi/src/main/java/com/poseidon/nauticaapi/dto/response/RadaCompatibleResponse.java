package com.poseidon.nauticaapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Rada libre donde cabe la nave, con la holgura y el % de aprovechamiento del amarre. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RadaCompatibleResponse {
    private Long id;
    private String codigo;
    private String muelle;
    private Double esloraMax;
    private Double mangaMax;
    private Double caladoMax;
    private Double tarifaMensual;
    private Double holguraEslora;
    private Double holguraManga;
    private Double aprovechamiento;
    private Long ranking;
}
