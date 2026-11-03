
package com.poseidon.nauticaapi.dto.response;

import com.poseidon.nauticaapi.entity.Rol;

public class LoginResponse {

    private String mensaje;
    private Long id;
    private String nombres;
    private String apellidos;
    private String correo;
    private Rol rol;

    public LoginResponse() {
    }

    public LoginResponse(
            String mensaje,
            Long id,
            String nombres,
            String apellidos,
            String correo,
            Rol rol) {

        this.mensaje = mensaje;
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.rol = rol;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
