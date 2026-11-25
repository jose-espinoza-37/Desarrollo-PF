package com.poseidon.nauticaapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Catálogo de la clasificación de pago que da la verificación con otros clubes. */
@Entity
@Table(name = "clasificacion_pago")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClasificacionPago {

    @Id
    @Column(length = 25)
    private String codigo;

    @Column(nullable = false, length = 60)
    private String descripcion;

    /** Qué pasa con la solicitud al elegir esta clasificación. */
    @Column(nullable = false, length = 200)
    private String detalle;

    /** true: la solicitud pasa a aprobación del Jefe (solo el pagador). */
    @Column(name = "apto", nullable = false)
    private Boolean apto;

    @Column(nullable = false)
    private Integer orden;
}
