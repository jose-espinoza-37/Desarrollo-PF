package com.poseidon.nauticaapi.validation;


public final class Patrones {

    private Patrones() {
    }


    public static final String NOMBRE = "^\\p{L}+(?:[ '\\-]\\p{L}+)*$";


    public static final String DNI = "^\\d{8}$";


    public static final String CELULAR = "^9\\d{8}$";


    public static final String PASSWORD = "^(?=.*[A-Za-z])(?=.*\\d)\\S{8,72}$";

    public static final String DIRECCION = "^[\\p{L}0-9\\s.,#°\\-/]+$";

    public static final String TEXTO = "^[\\p{L}0-9\\s.,;:¿?¡!()'\"\\-#/°]+$";


    public static final String CLASIFICACION = "^(PAGADOR|PAGADOR_ESPORADICO|RENUENTE)$";

    /** Texto libre que puede ir vacío (ej. observaciones). */
    public static final String TEXTO_OPCIONAL = "^[\\p{L}0-9\\s.,;:¿?¡!()'\"\\-#/°]*$";

    /** Nombre de la embarcación: letras, números, espacios y guiones. */
    public static final String NOMBRE_NAVE = "^[\\p{L}0-9]+(?:[ \\-][\\p{L}0-9]+)*$";

    /** Matrícula de la nave: dos letras, 4 a 6 dígitos y dos letras (ej. CO-12345-RE). */
    public static final String MATRICULA = "^[A-Z]{2}-\\d{4,6}-[A-Z]{2}$";

    /** Código de un catálogo (mayúsculas y guion bajo). */
    public static final String CODIGO_CATALOGO = "^[A-Z_]{2,20}$";
}
