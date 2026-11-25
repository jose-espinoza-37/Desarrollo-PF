package com.poseidon.nauticaapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/** Documento de la nave (matrícula o certificado) guardado en la BD como bytea. */
@Entity
@Table(name = "documentos_embarcacion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoEmbarcacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "embarcacion_id", nullable = false)
    @ToString.Exclude
    private Embarcacion embarcacion;

    /** MATRICULA, CERTIFICADO u OTRO. */
    @Column(name = "tipo_documento", nullable = false, length = 20)
    private String tipoDocumento;

    @Column(name = "nombre_archivo", nullable = false, length = 150)
    private String nombreArchivo;

    @Column(name = "content_type", nullable = false, length = 60)
    private String contentType;

    @Column(nullable = false)
    private Long tamano;

    @Column(nullable = false, columnDefinition = "bytea")
    @Basic(fetch = FetchType.LAZY)
    @ToString.Exclude
    private byte[] contenido;

    @Column(name = "fecha_carga", nullable = false)
    @Builder.Default
    private LocalDateTime fechaCarga = LocalDateTime.now();
}
