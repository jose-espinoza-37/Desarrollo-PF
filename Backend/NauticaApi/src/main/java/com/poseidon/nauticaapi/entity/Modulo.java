package com.poseidon.nauticaapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Opción del menú dinámico. Los registros se cargan desde db/data.sql. */
@Entity
@Table(name = "modulos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Modulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, unique = true, length = 120)
    private String ruta;

    @Column(length = 40)
    private String icono;

    @Column(nullable = false)
    private Integer orden;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
