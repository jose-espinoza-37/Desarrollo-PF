package com.poseidon.nauticaapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Catálogo de estados de la ficha de embarcación (pendiente, verificada, observada). */
@Entity
@Table(name = "estado_embarcacion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoEmbarcacion {

    @Id
    @Column(length = 20)
    private String codigo;

    @Column(nullable = false, length = 60)
    private String descripcion;

    @Column(nullable = false)
    private Integer orden;
}
