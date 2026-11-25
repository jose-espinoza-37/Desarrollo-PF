package com.poseidon.nauticaapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Solicitud de inscripción de un postulante (HU08 a HU10).
 * estado y clasificacion guardan el código de los catálogos
 * estado_solicitud y clasificacion_pago.
 */
@Entity
@Table(name = "solicitudes_socio", indexes = {
        @Index(name = "idx_solicitud_dni", columnList = "dni"),
        @Index(name = "idx_solicitud_estado", columnList = "estado")
})
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudSocio {

    public static final String PENDIENTE = "PENDIENTE";
    public static final String VERIFICADA = "VERIFICADA";
    public static final String NO_APTA = "NO_APTA";
    public static final String APROBADA = "APROBADA";
    public static final String DESAPROBADA = "DESAPROBADA";

    public static final String PAGADOR = "PAGADOR";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, updatable = false)
    @Builder.Default
    private UUID codigo = UUID.randomUUID();

    @Column(nullable = false, length = 80)
    private String nombres;

    @Column(nullable = false, length = 80)
    private String apellidos;

    @Column(nullable = false, length = 8)
    private String dni;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false, length = 150)
    private String correo;

    @Column(nullable = false, length = 9)
    private String celular;

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(name = "posee_embarcacion", nullable = false)
    private Boolean poseeEmbarcacion;

    @Column(name = "cantidad_embarcaciones", nullable = false)
    private Integer cantidadEmbarcaciones;

    @Column(name = "password_hash", nullable = false)
    @ToString.Exclude
    private String passwordHash;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = PENDIENTE;

    @Column(length = 25)
    private String clasificacion;

    @Column(name = "resultado_verificacion", length = 500)
    private String resultadoVerificacion;

    @Column(name = "motivo_desaprobacion", length = 500)
    private String motivoDesaprobacion;

    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    @Column(name = "fecha_verificacion")
    private LocalDateTime fechaVerificacion;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "verificado_por", length = 150)
    private String verificadoPor;

    @Column(name = "resuelto_por", length = 150)
    private String resueltoPor;

    @Column(name = "usuario_id")
    private Long usuarioId;
}
