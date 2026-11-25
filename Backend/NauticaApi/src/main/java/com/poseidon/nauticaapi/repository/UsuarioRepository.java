package com.poseidon.nauticaapi.repository;

import com.poseidon.nauticaapi.entity.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Trae el usuario con su rol y módulos en una sola consulta. */
    @EntityGraph(attributePaths = {"rol", "rol.modulos"})
    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}
