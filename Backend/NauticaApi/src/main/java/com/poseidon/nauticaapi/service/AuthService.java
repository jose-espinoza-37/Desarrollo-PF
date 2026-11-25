package com.poseidon.nauticaapi.service;

import com.poseidon.nauticaapi.auditoria.AuditoriaContext;
import com.poseidon.nauticaapi.auditoria.AuditoriaService;
import com.poseidon.nauticaapi.dto.request.LoginRequest;
import com.poseidon.nauticaapi.dto.request.RegistroRequest;
import com.poseidon.nauticaapi.dto.response.AuthResponse;
import com.poseidon.nauticaapi.entity.Rol;
import com.poseidon.nauticaapi.entity.Usuario;
import com.poseidon.nauticaapi.exception.CredencialesInvalidasException;
import com.poseidon.nauticaapi.repository.RolRepository;
import com.poseidon.nauticaapi.repository.UsuarioRepository;
import com.poseidon.nauticaapi.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private static final String MODULO = "Acceso";

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuditoriaService auditoriaService;

    @Transactional
    public Usuario registrarSocio(RegistroRequest request) {
        String correo = request.getCorreo().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        }
        Rol rol = rolRepository.findByCodigo(Rol.SOCIO)
                .orElseThrow(() -> new IllegalStateException("No existe el rol SOCIO en la base de datos."));

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombres(request.getNombres().trim())
                .apellidos(request.getApellidos().trim())
                .correo(correo)
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(rol)
                .build());

        // Auditoría: INSERT del usuario
        auditoriaService.registrarCreacion("public", "usuarios", usuario.getId().toString(), MODULO);
        return usuario;
    }

    /** Inicio de sesión (HU06). Los intentos correctos y fallidos quedan en la auditoría. */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String correo = request.getCorreo().trim().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .filter(u -> u.getActivo() && u.getRol() != null && u.getRol().getActivo())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
                .orElse(null);

        if (usuario == null) {
            auditoriaService.registrarEvento("usuarios", correo, MODULO, "LOGIN", false,
                    "Credenciales inválidas o usuario inactivo");
            throw new CredencialesInvalidasException();
        }

        // Auditoría: el evento queda a nombre del usuario que inicia sesión
        AuditoriaContext.setIdUsuario(usuario.getId());
        auditoriaService.registrarEvento("usuarios", usuario.getId().toString(), MODULO, "LOGIN", true, null);

        return AuthResponse.fromEntity(usuario, jwtService.generarToken(usuario));
    }
}
