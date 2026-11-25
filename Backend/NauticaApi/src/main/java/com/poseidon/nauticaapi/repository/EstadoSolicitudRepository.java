package com.poseidon.nauticaapi.repository;

import com.poseidon.nauticaapi.entity.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EstadoSolicitudRepository extends JpaRepository<EstadoSolicitud, String> {

    List<EstadoSolicitud> findAllByOrderByOrden();
}
