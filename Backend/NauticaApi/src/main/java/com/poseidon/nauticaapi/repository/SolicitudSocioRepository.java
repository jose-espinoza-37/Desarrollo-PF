package com.poseidon.nauticaapi.repository;

import com.poseidon.nauticaapi.entity.SolicitudSocio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SolicitudSocioRepository extends JpaRepository<SolicitudSocio, Long> {

    List<SolicitudSocio> findAllByOrderByFechaSolicitudDesc();

    List<SolicitudSocio> findByEstadoOrderByFechaSolicitudAsc(String estado);


    Optional<SolicitudSocio> findByCodigo(UUID codigo);


    boolean existsByDniAndEstadoIn(String dni, Collection<String> estados);

    boolean existsByCorreoAndEstadoIn(String correo, Collection<String> estados);


    long countByDni(String dni);

    long countByEstado(String estado);

    List<SolicitudSocio> findByFechaSolicitudGreaterThanEqual(LocalDateTime desde);

    @Query("SELECT s.estado, COUNT(s) FROM SolicitudSocio s GROUP BY s.estado")
    List<Object[]> contarPorEstado();

    @Query("SELECT s.clasificacion, COUNT(s) FROM SolicitudSocio s "
            + "WHERE s.clasificacion IS NOT NULL GROUP BY s.clasificacion")
    List<Object[]> contarPorClasificacion();
}
