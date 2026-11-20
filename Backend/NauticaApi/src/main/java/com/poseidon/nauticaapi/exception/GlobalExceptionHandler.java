
package com.poseidon.nauticaapi.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.poseidon.nauticaapi.exception.CredencialesInvalidasException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarErroresValidacion(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("fecha", LocalDateTime.now());
        respuesta.put("estado", HttpStatus.BAD_REQUEST.value());
        respuesta.put("error", "Error de validacion");
        respuesta.put("mensajes", errores);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(respuesta);
    }

    // Manejar solicitudes JSON mal formadas
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> manejarJsonInvalido(
            HttpMessageNotReadableException ex) {

        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "El contenido JSON enviado no es valido.");
    }

    // Manejar correos duplicados y conflictos de datos
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarArgumentoInvalido(
            IllegalArgumentException ex) {

        return crearRespuesta(
                HttpStatus.CONFLICT,
                ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> manejarConflictoDatos(
            DataIntegrityViolationException ex) {

        return crearRespuesta(
                HttpStatus.CONFLICT,
                "No se pudo guardar la informacion porque existe un conflicto con los datos.");
    }

    // Recurso inexistente (ej. solicitud con un id o código que no existe)
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarNoEncontrado(
            RecursoNoEncontradoException ex) {

        return crearRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Acción no permitida en el estado actual (ej. aprobar una solicitud pendiente)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> manejarEstadoInvalido(
            IllegalStateException ex) {

        return crearRespuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Manejar errores inesperados
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarErrorGeneral(
            Exception ex) {

        return crearRespuesta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrio un error interno en el servidor.");
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, Object>> manejarCredencialesInvalidas(
            CredencialesInvalidasException ex) {

        return crearRespuesta(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage());
    }

    // Metodo auxiliar para construir respuestas
    private ResponseEntity<Map<String, Object>> crearRespuesta(
            HttpStatus estado,
            String mensaje) {

        Map<String, Object> respuesta = new HashMap<>();

        respuesta.put("fecha", LocalDateTime.now());
        respuesta.put("estado", estado.value());
        respuesta.put("error", estado.getReasonPhrase());
        respuesta.put("mensaje", mensaje);

        return ResponseEntity
                .status(estado)
                .body(respuesta);
    }
}
