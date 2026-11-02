
package com.poseidon.nauticaapi.dto.response;

import com.poseidon.nauticaapi.entity.Rol;

public class AuthResponse {

    private String accessToken;
    private String tokenType;
    private Long id;
    private String nombres;
    private String apellidos;
    private String correo;
    private Rol rol;

    public AuthResponse(
            String accessToken,
            Long id,
            String nombres,
            String apellidos,
            String correo,
            Rol rol) {

        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.rol = rol;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Long getId() {
        return id;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getCorreo() {
        return correo;
    }

    public Rol getRol() {
        return rol;
    }
}
