package com.poseidon.nauticaapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroRequest {

    @NotBlank(message = "Los nombres son obligatorios.")
    @Size(max = 80, message = "Los nombres no pueden superar los 80 caracteres.")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios.")
    @Size(max = 80, message = "Los apellidos no pueden superar los 80 caracteres.")
    private String apellidos;

    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "Debe ingresar un correo electrónico válido.")
    @Size(max = 150, message = "El correo no puede superar los 150 caracteres.")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
    private String password;
}
