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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Nombre del atributo de la petición con el id del usuario autenticado. */
    public static final String ATRIBUTO_ID_USUARIO = "idUsuarioAutenticado";

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

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
                    .filter(usuario -> usuario.getActivo() && usuario.getRol() != null)
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

                        // El id queda en la petición para la auditoría (evita otra consulta)
                        request.setAttribute(ATRIBUTO_ID_USUARIO, usuario.getId());
                    });
        }


        filterChain.doFilter(request, response);
    }
}