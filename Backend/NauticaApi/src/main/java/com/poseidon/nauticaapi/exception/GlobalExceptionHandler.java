package com.poseidon.nauticaapi.exception;

import com.poseidon.nauticaapi.util.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Convierte las excepciones en un ErrorResponse con el código HTTP adecuado. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 400: errores de @Valid en los DTO, con el mensaje de cada campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarErroresValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .estado(HttpStatus.BAD_REQUEST.value())
                .error("Error de validación")
                .mensaje("Revisa los datos enviados.")
                .mensajes(errores)
                .build());
    }

    /** 400: parámetros fuera de rango (ej. dias=1000 en el dashboard) o con tipo incorrecto. */
    @ExceptionHandler({HandlerMethodValidationException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, MissingServletRequestPartException.class})
    public ResponseEntity<ErrorResponse> manejarParametroInvalido(Exception ex) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, "Uno de los parámetros enviados no es válido.");
    }

    /** 413: documento de embarcación mayor a 5 MB. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> manejarArchivoGrande(MaxUploadSizeExceededException ex) {
        return crearRespuesta(HttpStatus.PAYLOAD_TOO_LARGE, "El archivo no puede superar los 5 MB.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarJsonInvalido(HttpMessageNotReadableException ex) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, "El contenido JSON enviado no es válido.");
    }

    /** 401: login incorrecto. */
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> manejarCredencialesInvalidas(CredencialesInvalidasException ex) {
        return crearRespuesta(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /** 401: falta el token o no es válido (viene de RestAuthenticationEntryPoint). */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> manejarNoAutenticado(AuthenticationException ex) {
        return crearRespuesta(HttpStatus.UNAUTHORIZED, "Se requiere un token válido para acceder a este recurso.");
    }

    /** 403: el rol no tiene permiso (viene de RestAccessDeniedHandler). */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> manejarAccesoDenegado(AccessDeniedException ex) {
        return crearRespuesta(HttpStatus.FORBIDDEN, "No tienes permisos para realizar esta acción.");
    }

    /** 404: solicitud o código de seguimiento inexistente. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return crearRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** 409: reglas de negocio (DNI duplicado, menor de edad, estado incorrecto...). */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> manejarConflicto(RuntimeException ex) {
        return crearRespuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> manejarConflictoDatos(DataIntegrityViolationException ex) {
        return crearRespuesta(HttpStatus.CONFLICT,
                "No se pudo guardar la información porque existe un conflicto con los datos.");
    }

    /** 500: cualquier otro error inesperado (se escribe completo en la consola del backend). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarErrorGeneral(Exception ex) {
        log.error("Error no controlado: {}", ex.getMessage(), ex);
        return crearRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno en el servidor.");
    }

    private ResponseEntity<ErrorResponse> crearRespuesta(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(ErrorResponse.builder()
                .estado(estado.value())
                .error(estado.getReasonPhrase())
                .mensaje(mensaje)
                .build());
    }
}
