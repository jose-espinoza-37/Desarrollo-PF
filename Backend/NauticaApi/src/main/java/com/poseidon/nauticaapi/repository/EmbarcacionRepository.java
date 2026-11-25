package com.poseidon.nauticaapi.repository;

import com.poseidon.nauticaapi.entity.Embarcacion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmbarcacionRepository extends JpaRepository<Embarcacion, Long> {

    @EntityGraph(attributePaths = {"socio", "rada"})
    List<Embarcacion> findBySocio_IdOrderByFechaRegistroDesc(Long socioId);

    @EntityGraph(attributePaths = {"socio", "rada"})
    List<Embarcacion> findAllByOrderByFechaRegistroDesc();

    @EntityGraph(attributePaths = {"socio", "rada"})
    List<Embarcacion> findByEstadoOrderByFechaRegistroAsc(String estado);

    /** Embarcaciones verificadas que todavía no tienen rada (bandeja de HU13). */
    @EntityGraph(attributePaths = {"socio", "rada"})
    List<Embarcacion> findByEstadoAndRadaIsNullOrderByFechaVerificacionAsc(String estado);

    Optional<Embarcacion> findByRada_Id(Long radaId);

    /** Naves que ocupan una rada (para el plano de radas). */
    @EntityGraph(attributePaths = {"socio", "rada"})
    List<Embarcacion> findByRadaIsNotNull();

    boolean existsByMatricula(String matricula);

    boolean existsByMatriculaAndIdNot(String matricula, Long id);
}
