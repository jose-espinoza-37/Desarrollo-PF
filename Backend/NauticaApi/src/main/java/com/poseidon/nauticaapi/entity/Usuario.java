package com.poseidon.nauticaapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombres;

    @Column(nullable = false, length = 80)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(nullable = false)
    @ToString.Exclude
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id")
    private Rol rol;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
