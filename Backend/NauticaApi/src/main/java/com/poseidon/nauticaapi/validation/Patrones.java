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
}
