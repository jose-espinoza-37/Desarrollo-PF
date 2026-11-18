package com.poseidon.nauticaapi.config;

import com.poseidon.nauticaapi.security.JwtAuthenticationFilter;
import com.poseidon.nauticaapi.security.RestAccessDeniedHandler;
import com.poseidon.nauticaapi.security.RestAuthenticationEntryPoint;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler) throws Exception {

        http
                // CSRF deshabilitado: el token viaja en el encabezado Authorization,
                // no en una cookie que el navegador envíe automáticamente.
                .csrf(csrf -> csrf.disable())

                // Permitir que Angular (localhost:4200) consuma la API.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Reglas de acceso.
                .authorizeHttpRequests(auth -> auth
                        // Públicos: registro e inicio de sesión.
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/registro",
                                "/api/auth/login")
                        .permitAll()

                        // Ruta interna de Spring para mostrar errores.
                        .requestMatchers("/error").permitAll()

                        // Exclusivo de administradores.
                        .requestMatchers("/api/admin/**").hasRole("ADMINISTRADOR")

                        // Todo lo demás exige un usuario autenticado.
                        .anyRequest().authenticated())

                // Respuestas JSON para 401 y 403.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))

                // Sin autenticación básica ni formulario de login.
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())

                // La API no guarda sesiones: todo depende del token.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // El filtro JWT se ejecuta antes del filtro de usuario/contraseña.
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(List.of("http://localhost:4200"));
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", configuracion);
        return fuente;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}