package com.poseidon.nauticaapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Catálogo de tipos de embarcación (tabla tipo_embarcacion). */
@Entity
@Table(name = "tipo_embarcacion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoEmbarcacion {

    @Id
    @Column(length = 20)
    private String codigo;

    @Column(nullable = false, length = 60)
    private String descripcion;

    @Column(nullable = false)
    private Integer orden;
}
