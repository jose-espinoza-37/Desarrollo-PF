package com.poseidon.nauticaapi.repository;

import com.poseidon.nauticaapi.entity.TipoEmbarcacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoEmbarcacionRepository extends JpaRepository<TipoEmbarcacion, String> {

    List<TipoEmbarcacion> findAllByOrderByOrden();
}
