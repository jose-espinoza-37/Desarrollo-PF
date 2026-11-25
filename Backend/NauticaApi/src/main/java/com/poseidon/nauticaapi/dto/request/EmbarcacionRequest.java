package com.poseidon.nauticaapi.dto.request;

import com.poseidon.nauticaapi.validation.Patrones;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Ficha de "Registro de Embarcación" (HU11). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbarcacionRequest {

    @NotBlank(message = "El nombre de la embarcación es obligatorio.")
    @Size(min = 2, max = 60, message = "El nombre debe tener entre 2 y 60 caracteres.")
    @Pattern(regexp = Patrones.NOMBRE_NAVE, message = "El nombre solo puede contener letras, números, espacios y guiones.")
    private String nombre;

    @NotBlank(message = "La matrícula es obligatoria.")
    @Pattern(regexp = Patrones.MATRICULA, message = "La matrícula debe tener el formato XX-12345-XX (ej. CO-12345-RE).")
    private String matricula;

    @NotBlank(message = "El tipo de embarcación es obligatorio.")
    @Pattern(regexp = Patrones.CODIGO_CATALOGO, message = "Tipo de embarcación no válido.")
    private String tipo;

    @NotNull(message = "La eslora es obligatoria.")
    @DecimalMin(value = "2.00", message = "La eslora mínima es 2 m.")
    @DecimalMax(value = "40.00", message = "La eslora máxima es 40 m.")
    @Digits(integer = 3, fraction = 2, message = "La eslora admite hasta 2 decimales.")
    private BigDecimal eslora;

    @NotNull(message = "La manga es obligatoria.")
    @DecimalMin(value = "0.80", message = "La manga mínima es 0.80 m.")
    @DecimalMax(value = "12.00", message = "La manga máxima es 12 m.")
    @Digits(integer = 3, fraction = 2, message = "La manga admite hasta 2 decimales.")
    private BigDecimal manga;

    @NotNull(message = "El calado es obligatorio.")
    @DecimalMin(value = "0.20", message = "El calado mínimo es 0.20 m.")
    @DecimalMax(value = "6.00", message = "El calado máximo es 6 m.")
    @Digits(integer = 3, fraction = 2, message = "El calado admite hasta 2 decimales.")
    private BigDecimal calado;

    @NotNull(message = "El año de fabricación es obligatorio.")
    @Min(value = 1950, message = "El año de fabricación no puede ser anterior a 1950.")
    @Max(value = 2100, message = "Año de fabricación no válido.")
    private Integer anioFabricacion;
}
