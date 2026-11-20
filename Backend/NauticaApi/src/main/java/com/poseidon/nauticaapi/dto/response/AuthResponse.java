package com.poseidon.nauticaapi.dto.response;

import java.util.List;

public class AuthResponse {

    private String accessToken;
    private String tokenType;
    private Long id;
    private String nombres;
    private String apellidos;
    private String correo;
    private String rol;
    private String rolNombre;
    private List<ModuloResponse> modulos;

    public AuthResponse(
            String accessToken,
            Long id,
            String nombres,
            String apellidos,
            String correo,
            String rol,
            String rolNombre,
            List<ModuloResponse> modulos) {

        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.rol = rol;
        this.rolNombre = rolNombre;
        this.modulos = modulos;
    }

    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public Long getId() { return id; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getCorreo() { return correo; }
    public String getRol() { return rol; }
    public String getRolNombre() { return rolNombre; }
    public List<ModuloResponse> getModulos() { return modulos; }
}
