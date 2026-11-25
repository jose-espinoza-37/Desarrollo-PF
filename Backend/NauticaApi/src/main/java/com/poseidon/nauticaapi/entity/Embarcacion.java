package com.poseidon.nauticaapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Ficha de "Registro de Embarcación" de un socio (HU11). */
@Entity
@Table(name = "embarcaciones", indexes = @Index(name = "idx_embarcacion_estado", columnList = "estado"))
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Embarcacion {

    public static final String PENDIENTE = "PENDIENTE";
    public static final String VERIFICADA = "VERIFICADA";
    public static final String OBSERVADA = "OBSERVADA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Socio propietario. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "socio_id", nullable = false)
    @ToString.Exclude
    private Usuario socio;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, unique = true, length = 15)
    private String matricula;

    /** Código del catálogo tipo_embarcacion. */
    @Column(nullable = false, length = 20)
    private String tipo;

    /** Largo de la nave en metros. */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal eslora;

    /** Ancho de la nave en metros. */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal manga;

    /** Profundidad bajo el agua en metros. */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal calado;

    @Column(name = "anio_fabricacion", nullable = false)
    private Integer anioFabricacion;

    /** Código del catálogo estado_embarcacion. */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = PENDIENTE;

    @Column(length = 500)
    private String observaciones;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "fecha_verificacion")
    private LocalDateTime fechaVerificacion;

    @Column(name = "verificado_por", length = 150)
    private String verificadoPor;

    /** Rada asignada actualmente (null si no tiene). */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rada_id")
    @ToString.Exclude
    private Rada rada;
}
