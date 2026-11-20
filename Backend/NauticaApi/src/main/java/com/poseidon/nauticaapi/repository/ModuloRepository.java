package com.poseidon.nauticaapi.repository;

import com.poseidon.nauticaapi.entity.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModuloRepository extends JpaRepository<Modulo, Long> {

    Optional<Modulo> findByCodigo(String codigo);
}
