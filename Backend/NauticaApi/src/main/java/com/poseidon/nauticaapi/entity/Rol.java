package com.poseidon.nauticaapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Perfil de usuario. Cada rol tiene asignados los módulos que verá en su menú. */
@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rol {

    public static final String GERENTE = "GERENTE";
    public static final String ATENCION_CLIENTE = "ATENCION_CLIENTE";
    public static final String SERVICIOS_NAVIEROS = "SERVICIOS_NAVIEROS";
    public static final String ADMINISTRACION = "ADMINISTRACION";
    public static final String SOCIO = "SOCIO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "rol_modulo",
            joinColumns = @JoinColumn(name = "rol_id"),
            inverseJoinColumns = @JoinColumn(name = "modulo_id"))
    @Builder.Default
    private Set<Modulo> modulos = new HashSet<>();

    /** Módulos activos del rol, en el orden en que aparecen en el menú. */
    public List<Modulo> getModulosActivosOrdenados() {
        return modulos.stream()
                .filter(Modulo::getActivo)
                .sorted(Comparator.comparing(Modulo::getOrden))
                .toList();
    }
}
