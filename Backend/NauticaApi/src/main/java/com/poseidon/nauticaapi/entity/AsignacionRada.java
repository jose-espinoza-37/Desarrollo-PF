package com.poseidon.nauticaapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/** Historial de asignaciones y liberaciones de radas (HU13). */
@Entity
@Table(name = "asignaciones_rada")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionRada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rada_id", nullable = false)
    @ToString.Exclude
    private Rada rada;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "embarcacion_id", nullable = false)
    @ToString.Exclude
    private Embarcacion embarcacion;

    @Column(name = "fecha_asignacion", nullable = false)
    @Builder.Default
    private LocalDateTime fechaAsignacion = LocalDateTime.now();

    @Column(name = "asignado_por", nullable = false, length = 150)
    private String asignadoPor;

    @Column(name = "fecha_liberacion")
    private LocalDateTime fechaLiberacion;

    @Column(name = "liberado_por", length = 150)
    private String liberadoPor;

    @Column(name = "motivo_liberacion", length = 300)
    private String motivoLiberacion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activa = true;
}
