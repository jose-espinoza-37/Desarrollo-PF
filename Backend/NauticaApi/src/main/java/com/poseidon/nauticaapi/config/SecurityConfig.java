package com.poseidon.nauticaapi.config;

import com.poseidon.nauticaapi.security.JwtAuthenticationFilter;
import com.poseidon.nauticaapi.security.RestAccessDeniedHandler;
import com.poseidon.nauticaapi.security.RestAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String origenesPermitidos;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Autowired
    private RestAuthenticationEntryPoint authenticationEntryPoint;
    @Autowired
    private RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // El token viaja en el encabezado Authorization, no en una cookie: CSRF no aplica
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Públicos: inicio de sesión y registro
                        .requestMatchers(HttpMethod.POST, "/api/auth/registro", "/api/auth/login").permitAll()
                        // Públicos: solicitud de inscripción (HU08), su seguimiento y los catálogos
                        .requestMatchers(HttpMethod.POST, "/api/solicitudes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/solicitudes/seguimiento/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/catalogos/**").permitAll()
                        // Bandejas de verificación y aprobación (HU09, HU10)
                        .requestMatchers("/api/solicitudes/**").hasAnyRole("GERENTE", "ATENCION_CLIENTE")
                        // Panel gerencial (charts)
                        .requestMatchers("/api/dashboard/**").hasAnyRole("GERENTE", "ADMINISTRACION")
                        // HU11: el socio registra sus embarcaciones y documentos
                        .requestMatchers(HttpMethod.GET, "/api/embarcaciones/mias").hasRole("SOCIO")
                        .requestMatchers(HttpMethod.POST, "/api/embarcaciones", "/api/embarcaciones/*/documentos").hasRole("SOCIO")
                        .requestMatchers(HttpMethod.PUT, "/api/embarcaciones/*/verificar").hasAnyRole("SERVICIOS_NAVIEROS", "GERENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/embarcaciones/*").hasRole("SOCIO")
                        // Documentos: el servicio valida que el socio sea el dueño
                        .requestMatchers(HttpMethod.GET, "/api/embarcaciones/documentos/**")
                            .hasAnyRole("SOCIO", "SERVICIOS_NAVIEROS", "GERENTE")
                        // HU12 y HU13: Servicios Navieros (y Gerencia)
                        .requestMatchers(HttpMethod.GET, "/api/embarcaciones").hasAnyRole("SERVICIOS_NAVIEROS", "GERENTE")
                        .requestMatchers("/api/radas/**").hasAnyRole("SERVICIOS_NAVIEROS", "GERENTE")
                        // Bitácora de auditoría
                        .requestMatchers("/api/auditoria/**").hasRole("GERENTE")
                        // Estado de la API, Spring Boot Actuator y página de errores
                        .requestMatchers(HttpMethod.GET, "/").permitAll()
                        .requestMatchers("/actuator/**", "/error").permitAll()
                        // Todo lo demás exige un usuario autenticado
                        .anyRequest().authenticated())
                // Respuestas JSON para 401 y 403
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                // La API no guarda sesiones: todo depende del token
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // El filtro JWT se ejecuta antes del filtro de usuario/contraseña
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracion = new CorsConfiguration();
        // Orígenes permitidos separados por coma (local + dominio desplegado)
        configuracion.setAllowedOrigins(Arrays.stream(origenesPermitidos.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList());
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
