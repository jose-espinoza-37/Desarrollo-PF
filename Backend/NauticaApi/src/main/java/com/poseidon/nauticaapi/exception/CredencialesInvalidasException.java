package com.poseidon.nauticaapi.exception;

public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("El correo electrónico o la contraseña son incorrectos.");
    }
}