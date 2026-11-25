package com.poseidon.nauticaapi.repository.auditoria;

import com.poseidon.nauticaapi.entity.auditoria.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Repositorio de auditoria.evento. */
@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    List<Evento> findTop300ByOrderByIdEventoDesc();
}
