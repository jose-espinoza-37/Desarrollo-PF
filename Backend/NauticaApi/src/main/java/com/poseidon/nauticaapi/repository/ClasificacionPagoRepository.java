package com.poseidon.nauticaapi.repository;

import com.poseidon.nauticaapi.entity.ClasificacionPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClasificacionPagoRepository extends JpaRepository<ClasificacionPago, String> {

    List<ClasificacionPago> findAllByOrderByOrden();
}
