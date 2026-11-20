package com.poseidon.nauticaapi.controller;

import com.poseidon.nauticaapi.dto.response.ModuloResponse;
import com.poseidon.nauticaapi.entity.Usuario;
import com.poseidon.nauticaapi.exception.CredencialesInvalidasException;
import com.poseidon.nauticaapi.repository.UsuarioRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }


    @GetMapping("/usuarios/me")
    public ResponseEntity<Map<String, Object>> miPerfil(Authentication autenticacion) {

        Usuario usuario = buscarUsuario(autenticacion);

        Map<String, Object> perfil = new LinkedHashMap<>();
        perfil.put("id", usuario.getId());
        perfil.put("nombres", usuario.getNombres());
        perfil.put("apellidos", usuario.getApellidos());
        perfil.put("correo", usuario.getCorreo());
        perfil.put("rol", usuario.getRol().getCodigo());
        perfil.put("rolNombre", usuario.getRol().getNombre());
        perfil.put("fechaCreacion", usuario.getFechaCreacion());

        return ResponseEntity.ok(perfil);
    }


    @GetMapping("/usuarios/me/modulos")
    public ResponseEntity<List<ModuloResponse>> misModulos(Authentication autenticacion) {

        Usuario usuario = buscarUsuario(autenticacion);

        List<ModuloResponse> modulos = usuario.getRol().getModulosActivosOrdenados()
                .stream()
                .map(ModuloResponse::desde)
                .toList();

        return ResponseEntity.ok(modulos);
    }

    private Usuario buscarUsuario(Authentication autenticacion) {
        return usuarioRepository.findByCorreo(autenticacion.getName())
                .orElseThrow(CredencialesInvalidasException::new);
    }
}
