package com.poseidon.nauticaapi.security;

import com.poseidon.nauticaapi.repository.UsuarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UsuarioRepository usuarioRepository) {

        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. Leer el encabezado Authorization
        String encabezado = request.getHeader("Authorization");

        // 2. Sin encabezado o sin esquema Bearer: continuar sin autenticar
        if (encabezado == null || !encabezado.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer el token (lo que va después de "Bearer ")
        String token = encabezado.substring(7);

        // 4. Validar firma y vencimiento
        if (jwtService.esTokenValido(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String correo = jwtService.extraerCorreo(token);

            // 5. Consultar el estado actual del usuario en la base de datos
            usuarioRepository.findByCorreo(correo)
                    .filter(usuario -> usuario.isActivo())
                    .ifPresent(usuario -> {

                        // 6. Autoridad según el rol actual en la base de datos
                        SimpleGrantedAuthority autoridad = new SimpleGrantedAuthority(
                                "ROLE_" + usuario.getRol().name());

                        UsernamePasswordAuthenticationToken autenticacion = new UsernamePasswordAuthenticationToken(
                                usuario.getCorreo(),
                                null,
                                List.of(autoridad));

                        autenticacion.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request));

                        // 7. Registrar la autenticación en Spring Security
                        SecurityContextHolder.getContext().setAuthentication(autenticacion);
                    });
        }

        // 8. Continuar con el resto de la cadena
        filterChain.doFilter(request, response);
    }
}