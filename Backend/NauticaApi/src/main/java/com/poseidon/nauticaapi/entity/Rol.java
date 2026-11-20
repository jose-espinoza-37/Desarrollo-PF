package com.poseidon.nauticaapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "roles")
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
    private boolean activo = true;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "rol_modulo",
            joinColumns = @JoinColumn(name = "rol_id"),
            inverseJoinColumns = @JoinColumn(name = "modulo_id"))
    private Set<Modulo> modulos = new HashSet<>();

    public Rol() {
    }

    public Rol(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.activo = true;
    }


    public List<Modulo> getModulosActivosOrdenados() {
        return modulos.stream()
                .filter(Modulo::isActivo)
                .sorted(Comparator.comparing(Modulo::getOrden))
                .toList();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Set<Modulo> getModulos() { return modulos; }
    public void setModulos(Set<Modulo> modulos) { this.modulos = modulos; }
}
