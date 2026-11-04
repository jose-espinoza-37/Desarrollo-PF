
package com.poseidon.nauticaapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Deshabilitamos CSRF para las pruebas de nuestra API REST.
                .csrf(csrf -> csrf.disable())

                // Permitimos temporalmente todas las solicitudes.
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll())

                // Deshabilitamos autenticación HTTP Basic.
                .httpBasic(httpBasic -> httpBasic.disable())

                // Deshabilitamos el formulario de inicio de sesión.
                .formLogin(formLogin -> formLogin.disable())

                // No utilizaremos sesiones HTTP.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
