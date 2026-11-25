package com.poseidon.nauticaapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Catálogo de estados del flujo de inscripción (tabla estado_solicitud). */
@Entity
@Table(name = "estado_solicitud")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoSolicitud {

    @Id
    @Column(length = 20)
    private String codigo;

    @Column(nullable = false, length = 60)
    private String descripcion;

    @Column(nullable = false)
    private Integer orden;
}
