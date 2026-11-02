
package com.poseidon.nauticaapi.controller;

import com.poseidon.nauticaapi.dto.request.RegistroRequest;
import com.poseidon.nauticaapi.entity.Usuario;
import com.poseidon.nauticaapi.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    public ResponseEntity<Map<String, String>> registrarSocio(
            @Valid @RequestBody RegistroRequest request) {

        Usuario usuario = authService.registrarSocio(
                request.getNombres(),
                request.getApellidos(),
                request.getCorreo(),
                request.getPassword());

        Map<String, String> respuesta = Map.of(
                "mensaje", "Socio registrado correctamente.",
                "correo", usuario.getCorreo());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }
}
