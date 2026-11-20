package com.poseidon.nauticaapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "solicitudes_socio", indexes = {
        @Index(name = "idx_solicitud_dni", columnList = "dni"),
        @Index(name = "idx_solicitud_estado", columnList = "estado")
})
public class SolicitudSocio {


    public static final String PENDIENTE = "PENDIENTE";
    public static final String VERIFICADA = "VERIFICADA";
    public static final String NO_APTA = "NO_APTA";
    public static final String APROBADA = "APROBADA";
    public static final String DESAPROBADA = "DESAPROBADA";


    public static final String PAGADOR = "PAGADOR";
    public static final String PAGADOR_ESPORADICO = "PAGADOR_ESPORADICO";
    public static final String RENUENTE = "RENUENTE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(unique = true, updatable = false)
    private UUID codigo;


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
    private boolean poseeEmbarcacion;

    @Column(name = "cantidad_embarcaciones", nullable = false)
    private int cantidadEmbarcaciones;


    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 20)
    private String estado = PENDIENTE;

    @Column(length = 25)
    private String clasificacion;

    @Column(name = "resultado_verificacion", length = 500)
    private String resultadoVerificacion;

    @Column(name = "motivo_desaprobacion", length = 500)
    private String motivoDesaprobacion;

    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud;

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

    @PrePersist
    protected void alCrear() {
        if (codigo == null) {
            codigo = UUID.randomUUID();
        }
        if (fechaSolicitud == null) {
            fechaSolicitud = LocalDateTime.now();
        }
    }

    public SolicitudSocio() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getCodigo() { return codigo; }
    public void setCodigo(UUID codigo) { this.codigo = codigo; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public boolean isPoseeEmbarcacion() { return poseeEmbarcacion; }
    public void setPoseeEmbarcacion(boolean poseeEmbarcacion) { this.poseeEmbarcacion = poseeEmbarcacion; }

    public int getCantidadEmbarcaciones() { return cantidadEmbarcaciones; }
    public void setCantidadEmbarcaciones(int cantidadEmbarcaciones) { this.cantidadEmbarcaciones = cantidadEmbarcaciones; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getClasificacion() { return clasificacion; }
    public void setClasificacion(String clasificacion) { this.clasificacion = clasificacion; }

    public String getResultadoVerificacion() { return resultadoVerificacion; }
    public void setResultadoVerificacion(String resultadoVerificacion) { this.resultadoVerificacion = resultadoVerificacion; }

    public String getMotivoDesaprobacion() { return motivoDesaprobacion; }
    public void setMotivoDesaprobacion(String motivoDesaprobacion) { this.motivoDesaprobacion = motivoDesaprobacion; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public LocalDateTime getFechaVerificacion() { return fechaVerificacion; }
    public void setFechaVerificacion(LocalDateTime fechaVerificacion) { this.fechaVerificacion = fechaVerificacion; }

    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(LocalDateTime fechaResolucion) { this.fechaResolucion = fechaResolucion; }

    public String getVerificadoPor() { return verificadoPor; }
    public void setVerificadoPor(String verificadoPor) { this.verificadoPor = verificadoPor; }

    public String getResueltoPor() { return resueltoPor; }
    public void setResueltoPor(String resueltoPor) { this.resueltoPor = resueltoPor; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
}
