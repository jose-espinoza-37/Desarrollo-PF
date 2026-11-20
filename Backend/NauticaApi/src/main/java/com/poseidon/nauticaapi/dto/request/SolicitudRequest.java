package com.poseidon.nauticaapi.dto.request;

import com.poseidon.nauticaapi.validation.Patrones;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;


public record SolicitudRequest(

        @NotBlank(message = "Los nombres son obligatorios.")
        @Size(min = 2, max = 80, message = "Los nombres deben tener entre 2 y 80 caracteres.")
        @Pattern(regexp = Patrones.NOMBRE, message = "Los nombres solo pueden contener letras y espacios.")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios.")
        @Size(min = 2, max = 80, message = "Los apellidos deben tener entre 2 y 80 caracteres.")
        @Pattern(regexp = Patrones.NOMBRE, message = "Los apellidos solo pueden contener letras y espacios.")
        String apellidos,

        @NotBlank(message = "El DNI es obligatorio.")
        @Pattern(regexp = Patrones.DNI, message = "El DNI debe tener exactamente 8 dígitos.")
        String dni,

        @NotNull(message = "La fecha de nacimiento es obligatoria.")
        @Past(message = "La fecha de nacimiento debe ser una fecha pasada.")
        LocalDate fechaNacimiento,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "Debe ingresar un correo electrónico válido.")
        @Size(max = 150, message = "El correo no puede superar los 150 caracteres.")
        String correo,

        @NotBlank(message = "El celular es obligatorio.")
        @Pattern(regexp = Patrones.CELULAR, message = "El celular debe tener 9 dígitos y empezar con 9.")
        String celular,

        @NotBlank(message = "La dirección es obligatoria.")
        @Size(min = 5, max = 200, message = "La dirección debe tener entre 5 y 200 caracteres.")
        @Pattern(regexp = Patrones.DIRECCION, message = "La dirección contiene caracteres no permitidos.")
        String direccion,

        boolean poseeEmbarcacion,

        @Min(value = 0, message = "La cantidad de embarcaciones no puede ser negativa.")
        @Max(value = 20, message = "La cantidad máxima de embarcaciones es 20.")
        int cantidadEmbarcaciones,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Pattern(regexp = Patrones.PASSWORD,
                message = "La contraseña debe tener entre 8 y 72 caracteres, con al menos una letra y un número.")
        String password) {
}
