package com.poseidon.nauticaapi.controller;

import com.poseidon.nauticaapi.entity.Usuario;
import com.poseidon.nauticaapi.exception.CredencialesInvalidasException;
import com.poseidon.nauticaapi.repository.UsuarioRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Cualquier usuario autenticado puede consultar su propio perfil.
    @GetMapping("/usuarios/me")
    public ResponseEntity<Map<String, Object>> miPerfil(Authentication autenticacion) {

        Usuario usuario = usuarioRepository.findByCorreo(autenticacion.getName())
                .orElseThrow(CredencialesInvalidasException::new);

        Map<String, Object> perfil = Map.of(
                "id", usuario.getId(),
                "nombres", usuario.getNombres(),
                "apellidos", usuario.getApellidos(),
                "correo", usuario.getCorreo(),
                "rol", usuario.getRol().name());

        return ResponseEntity.ok(perfil);
    }
}