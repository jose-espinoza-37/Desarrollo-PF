package com.poseidon.nauticaapi.service;

import com.poseidon.nauticaapi.dto.response.AuthResponse;
import com.poseidon.nauticaapi.dto.response.ModuloResponse;
import com.poseidon.nauticaapi.entity.Rol;
import com.poseidon.nauticaapi.entity.Usuario;
import com.poseidon.nauticaapi.exception.CredencialesInvalidasException;
import com.poseidon.nauticaapi.repository.RolRepository;
import com.poseidon.nauticaapi.repository.UsuarioRepository;
import com.poseidon.nauticaapi.security.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public Usuario registrarSocio(
            String nombres,
            String apellidos,
            String correo,
            String password) {

        return crearUsuario(nombres, apellidos, correo, password, Rol.SOCIO);
    }


    @Transactional
    public Usuario crearUsuario(
            String nombres,
            String apellidos,
            String correo,
            String password,
            String codigoRol) {


        correo = correo.trim().toLowerCase(Locale.ROOT);


        if (usuarioRepository.existsByCorreo(correo)) {
            throw new IllegalArgumentException(
                    "El correo electrónico ya está registrado.");
        }

        Rol rol = rolRepository.findByCodigo(codigoRol)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el rol " + codigoRol + " en la base de datos."));

        Usuario usuario = new Usuario();
        usuario.setNombres(nombres.trim());
        usuario.setApellidos(apellidos.trim());
        usuario.setCorreo(correo);


        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(rol);

        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(String correo, String password) {

        correo = correo.trim().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(CredencialesInvalidasException::new);

        if (!usuario.isActivo() || usuario.getRol() == null || !usuario.getRol().isActivo()) {
            throw new CredencialesInvalidasException();
        }

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new CredencialesInvalidasException();
        }

        String token = jwtService.generarToken(usuario);

        List<ModuloResponse> modulos = usuario.getRol().getModulosActivosOrdenados()
                .stream()
                .map(ModuloResponse::desde)
                .toList();

        return new AuthResponse(
                token,
                usuario.getId(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getCorreo(),
                usuario.getRol().getCodigo(),
                usuario.getRol().getNombre(),
                modulos);
    }
}
