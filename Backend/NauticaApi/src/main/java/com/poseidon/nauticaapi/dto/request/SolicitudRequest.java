package com.poseidon.nauticaapi.dto.request;

import com.poseidon.nauticaapi.entity.SolicitudSocio;
import com.poseidon.nauticaapi.validation.Patrones;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Locale;

/** Datos del formulario "Hazte socio" (HU08). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudRequest {

    @NotBlank(message = "Los nombres son obligatorios.")
    @Size(min = 2, max = 80, message = "Los nombres deben tener entre 2 y 80 caracteres.")
    @Pattern(regexp = Patrones.NOMBRE, message = "Los nombres solo pueden contener letras y espacios.")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios.")
    @Size(min = 2, max = 80, message = "Los apellidos deben tener entre 2 y 80 caracteres.")
    @Pattern(regexp = Patrones.NOMBRE, message = "Los apellidos solo pueden contener letras y espacios.")
    private String apellidos;

    @NotBlank(message = "El DNI es obligatorio.")
    @Pattern(regexp = Patrones.DNI, message = "El DNI debe tener exactamente 8 dígitos.")
    private String dni;

    @NotNull(message = "La fecha de nacimiento es obligatoria.")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada.")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "Debe ingresar un correo electrónico válido.")
    @Size(max = 150, message = "El correo no puede superar los 150 caracteres.")
    private String correo;

    @NotBlank(message = "El celular es obligatorio.")
    @Pattern(regexp = Patrones.CELULAR, message = "El celular debe tener 9 dígitos y empezar con 9.")
    private String celular;

    @NotBlank(message = "La dirección es obligatoria.")
    @Size(min = 5, max = 200, message = "La dirección debe tener entre 5 y 200 caracteres.")
    @Pattern(regexp = Patrones.DIRECCION, message = "La dirección contiene caracteres no permitidos.")
    private String direccion;

    @NotNull(message = "Indica si posees embarcaciones.")
    private Boolean poseeEmbarcacion;

    @Min(value = 0, message = "La cantidad de embarcaciones no puede ser negativa.")
    @Max(value = 20, message = "La cantidad máxima de embarcaciones es 20.")
    private Integer cantidadEmbarcaciones;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Pattern(regexp = Patrones.PASSWORD,
            message = "La contraseña debe tener entre 8 y 72 caracteres, con al menos una letra y un número.")
    private String password;

    /** Convierte el formulario en la entidad (la contraseña se encripta en el servicio). */
    public static SolicitudSocio toEntity(SolicitudRequest req) {
        boolean posee = Boolean.TRUE.equals(req.getPoseeEmbarcacion());
        return SolicitudSocio.builder()
                .nombres(req.getNombres().trim())
                .apellidos(req.getApellidos().trim())
                .dni(req.getDni().trim())
                .fechaNacimiento(req.getFechaNacimiento())
                .correo(req.getCorreo().trim().toLowerCase(Locale.ROOT))
                .celular(req.getCelular().trim())
                .direccion(req.getDireccion().trim())
                .poseeEmbarcacion(posee)
                .cantidadEmbarcaciones(posee && req.getCantidadEmbarcaciones() != null ? req.getCantidadEmbarcaciones() : 0)
                .estado(SolicitudSocio.PENDIENTE)
                .build();
    }
}
