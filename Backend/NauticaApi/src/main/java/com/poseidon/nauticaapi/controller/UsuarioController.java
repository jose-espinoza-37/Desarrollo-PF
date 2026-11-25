package com.poseidon.nauticaapi.controller;

import com.poseidon.nauticaapi.dto.response.ModuloResponse;
import com.poseidon.nauticaapi.dto.response.PerfilResponse;
import com.poseidon.nauticaapi.entity.Usuario;
import com.poseidon.nauticaapi.exception.CredencialesInvalidasException;
import com.poseidon.nauticaapi.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/me")
    public ResponseEntity<PerfilResponse> miPerfil(Authentication autenticacion) {
        return ResponseEntity.ok(PerfilResponse.fromEntity(buscarUsuario(autenticacion)));
    }

    @GetMapping("/me/modulos")
    public ResponseEntity<List<ModuloResponse>> misModulos(Authentication autenticacion) {
        return ResponseEntity.ok(ModuloResponse.fromEntities(
                buscarUsuario(autenticacion).getRol().getModulosActivosOrdenados()));
    }

    private Usuario buscarUsuario(Authentication autenticacion) {
        return usuarioRepository.findByCorreo(autenticacion.getName())
                .orElseThrow(CredencialesInvalidasException::new);
    }
}
