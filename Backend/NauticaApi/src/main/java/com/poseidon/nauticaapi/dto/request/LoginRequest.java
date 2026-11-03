
package com.poseidon.nauticaapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {

    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "Debe ingresar un correo electrónico válido.")
    @Size(max = 150, message = "El correo no puede superar los 150 caracteres.")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(max = 72, message = "La contraseña no puede superar los 72 caracteres.")
    private String password;

    public LoginRequest() {
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
