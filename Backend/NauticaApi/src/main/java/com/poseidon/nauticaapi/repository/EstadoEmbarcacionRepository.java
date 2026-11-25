package com.poseidon.nauticaapi.repository;

import com.poseidon.nauticaapi.entity.EstadoEmbarcacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EstadoEmbarcacionRepository extends JpaRepository<EstadoEmbarcacion, String> {

    List<EstadoEmbarcacion> findAllByOrderByOrden();
}
