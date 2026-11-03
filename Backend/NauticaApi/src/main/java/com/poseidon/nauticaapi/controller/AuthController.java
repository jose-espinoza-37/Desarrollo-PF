
package com.poseidon.nauticaapi.controller;

import com.poseidon.nauticaapi.dto.request.LoginRequest;
import com.poseidon.nauticaapi.dto.request.RegistroRequest;
import com.poseidon.nauticaapi.dto.response.LoginResponse;
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

        // REGISTRO DE SOCIOS
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

        // INICIO DE SESIÓN
        @PostMapping("/login")
        public ResponseEntity<LoginResponse> iniciarSesion(
                        @Valid @RequestBody LoginRequest request) {

                Usuario usuario = authService.login(
                                request.getCorreo(),
                                request.getPassword());

                LoginResponse respuesta = new LoginResponse(
                                "Inicio de sesión exitoso.",
                                usuario.getId(),
                                usuario.getNombres(),
                                usuario.getApellidos(),
                                usuario.getCorreo(),
                                usuario.getRol());

                return ResponseEntity.ok(respuesta);
        }
}
