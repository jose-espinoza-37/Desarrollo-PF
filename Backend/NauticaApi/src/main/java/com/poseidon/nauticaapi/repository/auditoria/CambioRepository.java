package com.poseidon.nauticaapi.repository.auditoria;

import com.poseidon.nauticaapi.entity.auditoria.Cambio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/** Repositorio de auditoria.cambio. */
@Repository
public interface CambioRepository extends JpaRepository<Cambio, Long> {

    List<Cambio> findByEvento_IdEventoOrderByIdCambio(Long idEvento);

    /** Cambios de varios eventos a la vez. */
    List<Cambio> findByEvento_IdEventoInOrderByIdCambio(Collection<Long> idsEvento);
}
