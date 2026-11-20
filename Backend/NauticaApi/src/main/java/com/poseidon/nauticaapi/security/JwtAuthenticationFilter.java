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


        String encabezado = request.getHeader("Authorization");


        if (encabezado == null || !encabezado.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }


        String token = encabezado.substring(7);


        if (jwtService.esTokenValido(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String correo = jwtService.extraerCorreo(token);


            usuarioRepository.findByCorreo(correo)
                    .filter(usuario -> usuario.isActivo() && usuario.getRol() != null)
                    .ifPresent(usuario -> {


                        SimpleGrantedAuthority autoridad = new SimpleGrantedAuthority(
                                "ROLE_" + usuario.getRol().getCodigo());

                        UsernamePasswordAuthenticationToken autenticacion = new UsernamePasswordAuthenticationToken(
                                usuario.getCorreo(),
                                null,
                                List.of(autoridad));

                        autenticacion.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request));


                        SecurityContextHolder.getContext().setAuthentication(autenticacion);
                    });
        }


        filterChain.doFilter(request, response);
    }
}