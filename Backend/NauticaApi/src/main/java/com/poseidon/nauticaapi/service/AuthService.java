package com.poseidon.nauticaapi.service;

import com.poseidon.nauticaapi.dto.response.AuthResponse;
import com.poseidon.nauticaapi.entity.Rol;
import com.poseidon.nauticaapi.entity.Usuario;
import com.poseidon.nauticaapi.exception.CredencialesInvalidasException;
import com.poseidon.nauticaapi.repository.UsuarioRepository;
import com.poseidon.nauticaapi.security.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public Usuario registrarSocio(
            String nombres,
            String apellidos,
            String correo,
            String password) {

        // Normalizar el correo
        correo = correo.trim().toLowerCase(Locale.ROOT);

        // Comprobar si el correo ya está registrado
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new IllegalArgumentException(
                    "El correo electrónico ya está registrado.");
        }

        // Crear el nuevo usuario
        Usuario usuario = new Usuario();

        usuario.setNombres(nombres.trim());
        usuario.setApellidos(apellidos.trim());
        usuario.setCorreo(correo);

        // Encriptar la contraseña mediante BCrypt
        usuario.setPassword(passwordEncoder.encode(password));

        // Todo registro público será inicialmente un socio
        usuario.setRol(Rol.SOCIO);

        // Guardar el usuario en PostgreSQL
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(String correo, String password) {

        // Normalizar el correo
        correo = correo.trim().toLowerCase(Locale.ROOT);

        // Buscar al usuario por su correo
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(CredencialesInvalidasException::new);

        // Verificar si la cuenta está activa
        if (!usuario.isActivo()) {
            throw new CredencialesInvalidasException();
        }

        // Comparar la contraseña ingresada con el hash BCrypt
        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new CredencialesInvalidasException();
        }

        // Credenciales correctas: generar el JWT
        String token = jwtService.generarToken(usuario);

        // Devolver el token junto con los datos públicos del usuario
        return new AuthResponse(
                token,
                usuario.getId(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getCorreo(),
                usuario.getRol());
    }
}