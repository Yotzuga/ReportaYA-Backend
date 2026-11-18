package com.ulima.incidenciaurbana.config;

import com.ulima.incidenciaurbana.exception.ForbiddenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Traduce los errores de validacion de {@code @Valid} en una respuesta JSON
 * limpia {@code {"error": "<mensaje>"}}, para que el cliente muestre el motivo
 * real (ej. "El comentario debe tener entre 10 y 1000 caracteres") en vez de un
 * "Bad Request" generico. Aplica a todos los endpoints con @Valid.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .filter(m -> m != null && !m.isBlank())
                .findFirst()
                .orElse("Datos invalidos en la solicitud");
        return ResponseEntity.badRequest().body(Map.of("error", mensaje));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<?> handleForbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    // [local-dev] Sin esto Spring responde {"error":"Unauthorized"/"Forbidden"} y la app
    // muestra ese texto en ingles en lugar del mensaje real del login.
    @ExceptionHandler(com.ulima.incidenciaurbana.exception.InvalidCredentialsException.class)
    public ResponseEntity<?> handleInvalidCredentials(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(com.ulima.incidenciaurbana.exception.EmailNotVerifiedException.class)
    public ResponseEntity<?> handleEmailNotVerified(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }

    /** Parametro con tipo incorrecto, p. ej. ?estado=FOO o un id no numerico. */
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Valor invalido para '" + ex.getName() + "': " + ex.getValue()));
    }

    /** Cuerpo JSON mal formado o ausente. */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleUnreadable(Exception ex) {
        return ResponseEntity.badRequest().body(Map.of("error", "El cuerpo de la solicitud no es un JSON valido"));
    }

    /** Parametro obligatorio ausente, p. ej. ?motivo= en rechazar. */
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(
            org.springframework.web.bind.MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", "Falta el parametro '" + ex.getParameterName() + "'"));
    }

    /** Violacion de restricciones de BD que se escape a las validaciones previas. */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(Exception ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Los datos no son validos o ya existen. Revisa la informacion enviada."));
    }
}
