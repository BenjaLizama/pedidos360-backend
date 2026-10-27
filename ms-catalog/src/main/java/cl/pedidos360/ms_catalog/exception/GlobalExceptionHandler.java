package cl.pedidos360.ms_catalog.exception;

import cl.pedidos360.ms_catalog.dto.response.StandardErrorResponse;
import cl.pedidos360.ms_catalog.enums.ErrorCode;
import cl.pedidos360.ms_catalog.mapper.ErrorResponseMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.TreeMap;

/**
 * Manejador global de excepciones de la aplicación.
 *
 * <p>Centraliza el tratamiento de errores producidos por los controladores,
 * servicios y capa de persistencia, transformándolos en respuestas HTTP
 * consistentes mediante {@link StandardErrorResponse}.</p>
 *
 * <p>Las excepciones de autenticación y autorización son manejadas por
 * los componentes correspondientes de Spring Security.</p>
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorResponseMapper errorResponseMapper;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Recurso no encontrado | path={} | method={} | message={}",
                request.getRequestURI(),
                request.getMethod(),
                exception.getMessage()
        );

        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getErrorCode(),
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<StandardErrorResponse> handleResourceAlreadyExists(
            ResourceAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Recurso ya existente | path={} | method={} | message={}",
                request.getRequestURI(),
                request.getMethod(),
                exception.getMessage()
        );

        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getErrorCode(),
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<StandardErrorResponse> handleInsufficientStock(
            InsufficientStockException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Stock insuficiente | path={} | method={} | message={}",
                request.getRequestURI(),
                request.getMethod(),
                exception.getMessage()
        );

        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getErrorCode(),
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<StandardErrorResponse> handleInvalidOperation(
            InvalidOperationException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Operación inválida | path={} | method={} | message={}",
                request.getRequestURI(),
                request.getMethod(),
                exception.getMessage()
        );

        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getErrorCode(),
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<StandardErrorResponse> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Error de negocio | path={} | method={} | code={} | message={}",
                request.getRequestURI(),
                request.getMethod(),
                exception.getErrorCode(),
                exception.getMessage()
        );

        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getErrorCode(),
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<StandardErrorResponse> handleOptimisticLockingFailure(
            OptimisticLockingFailureException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Conflicto de concurrencia optimista | path={} | method={}",
                request.getRequestURI(),
                request.getMethod()
        );

        return buildResponse(
                HttpStatus.CONFLICT,
                ErrorCode.OPTIMISTIC_LOCK_CONFLICT,
                "El recurso fue modificado por otro proceso. "
                        + "Vuelve a consultar el recurso e inténtalo nuevamente.",
                request
        );
    }

    /**
     * Maneja errores de Bean Validation.
     *
     * <p>Si un campo tiene varias restricciones incumplidas,
     * conserva el primer mensaje para mantener una respuesta concisa.</p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> validationErrors = new TreeMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        validationErrors.putIfAbsent(
                                error.getField(),
                                error.getDefaultMessage() != null
                                        ? error.getDefaultMessage()
                                        : "El valor proporcionado no es válido."
                        )
                );

        log.warn(
                "Error de validación | path={} | method={} | fields={}",
                request.getRequestURI(),
                request.getMethod(),
                validationErrors.keySet()
        );

        StandardErrorResponse response =
                errorResponseMapper.toValidationResponse(
                        validationErrors,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Maneja parámetros que no pueden convertirse al tipo esperado.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<StandardErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        String message = String.format(
                "El parámetro '%s' tiene un formato inválido.",
                exception.getName()
        );

        log.warn(
                "Parámetro inválido | path={} | method={} | parameter={}",
                request.getRequestURI(),
                request.getMethod(),
                exception.getName()
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                message,
                request
        );
    }

    /**
     * Maneja cuerpos JSON vacíos, mal formados o incompatibles
     * con los tipos esperados por la petición.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<StandardErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Cuerpo de petición ilegible | path={} | method={}",
                request.getRequestURI(),
                request.getMethod()
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "El cuerpo de la petición está vacío o contiene datos "
                        + "inválidos. Revisa el formato enviado.",
                request
        );
    }

    /**
     * Maneja conflictos con índices únicos de MongoDB.
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<StandardErrorResponse> handleDuplicateKey(
            DuplicateKeyException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Conflicto de clave duplicada | path={} | method={}",
                request.getRequestURI(),
                request.getMethod()
        );

        return buildResponse(
                HttpStatus.CONFLICT,
                ErrorCode.RESOURCE_ALREADY_EXISTS,
                "El recurso que intenta crear o modificar ya existe.",
                request
        );
    }

    /**
     * Fallback para excepciones inesperadas.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error(
                "Error inesperado | path={} | method={}",
                request.getRequestURI(),
                request.getMethod(),
                exception
        );

        StandardErrorResponse response =
                errorResponseMapper.toInternalServerError(
                        exception,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * Construye una respuesta estándar para errores controlados.
     */
    private ResponseEntity<StandardErrorResponse> buildResponse(
            HttpStatus status,
            ErrorCode errorCode,
            String message,
            HttpServletRequest request
    ) {
        StandardErrorResponse response =
                errorResponseMapper.toResponse(
                        status,
                        errorCode,
                        message,
                        request
                );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}
