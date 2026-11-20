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

import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String origenesPermitidos;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler) throws Exception {

        http

                .csrf(csrf -> csrf.disable())

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))


                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/registro",
                                "/api/auth/login")
                        .permitAll()


                        .requestMatchers(HttpMethod.POST, "/api/solicitudes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/solicitudes/seguimiento/**").permitAll()


                        .requestMatchers("/api/solicitudes/**").hasAnyRole("GERENTE", "ATENCION_CLIENTE")

                        .requestMatchers("/api/dashboard/**").hasAnyRole("GERENTE", "ADMINISTRACION")


                        .requestMatchers("/error").permitAll()

                        .requestMatchers("/api/admin/**").hasAnyRole("GERENTE", "ADMINISTRACION")

                        // Todo lo demás exige un usuario autenticado.
                        .anyRequest().authenticated())

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))


                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())


                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))


                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuracion = new CorsConfiguration();

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