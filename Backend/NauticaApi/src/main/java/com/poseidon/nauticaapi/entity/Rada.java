package com.poseidon.nauticaapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Lugar de amarre del club. Las medidas máximas indican qué naves caben. */
@Entity
@Table(name = "radas")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Rada {

    public static final String LIBRE = "LIBRE";
    public static final String OCUPADA = "OCUPADA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(nullable = false, length = 1)
    private String muelle;

    @Column(name = "eslora_max", nullable = false, precision = 5, scale = 2)
    private BigDecimal esloraMax;

    @Column(name = "manga_max", nullable = false, precision = 5, scale = 2)
    private BigDecimal mangaMax;

    @Column(name = "calado_max", nullable = false, precision = 5, scale = 2)
    private BigDecimal caladoMax;

    @Column(name = "tarifa_mensual", nullable = false, precision = 8, scale = 2)
    private BigDecimal tarifaMensual;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String estado = LIBRE;
}
