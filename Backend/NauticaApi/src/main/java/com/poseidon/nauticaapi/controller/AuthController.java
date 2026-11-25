package com.poseidon.nauticaapi.controller;

import com.poseidon.nauticaapi.dto.request.LoginRequest;
import com.poseidon.nauticaapi.dto.request.RegistroRequest;
import com.poseidon.nauticaapi.dto.response.AuthResponse;
import com.poseidon.nauticaapi.entity.Usuario;
import com.poseidon.nauticaapi.service.AuthService;
import com.poseidon.nauticaapi.util.ObjectResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/registro")
    public ResponseEntity<ObjectResponse<String>> registrarSocio(@Valid @RequestBody RegistroRequest request) {
        Usuario usuario = authService.registrarSocio(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ObjectResponse.<String>builder()
                .success(true)
                .message("Socio registrado correctamente.")
                .object(usuario.getCorreo())
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> iniciarSesion(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
