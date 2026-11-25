package com.poseidon.nauticaapi.repository;

import com.poseidon.nauticaapi.entity.AsignacionRada;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AsignacionRadaRepository extends JpaRepository<AsignacionRada, Long> {

    Optional<AsignacionRada> findByEmbarcacion_IdAndActivaTrue(Long embarcacionId);

    Optional<AsignacionRada> findByRada_IdAndActivaTrue(Long radaId);

    /** Asignaciones vigentes de varias naves, en una sola consulta. */
    List<AsignacionRada> findByEmbarcacion_IdInAndActivaTrue(Collection<Long> embarcacionIds);

    @EntityGraph(attributePaths = {"rada", "embarcacion", "embarcacion.socio"})
    List<AsignacionRada> findTop200ByOrderByIdDesc();
}
