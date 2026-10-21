package cl.pedidos360.ms_orders.exception;

import cl.pedidos360.ms_orders.dto.response.StandardErrorResponse;
import cl.pedidos360.ms_orders.enums.ErrorCode;
import cl.pedidos360.ms_orders.mapper.ErrorResponseMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.TreeMap;

/**
 * Manejador global de excepciones de ms-orders.
 *
 * Centraliza el tratamiento de errores producidos por los controladores,
 * servicios y capa de persistencia, entregando respuestas HTTP consistentes
 * mediante StandardErrorResponse.
 *
 * Las excepciones de autenticación y autorización son manejadas por
 * los componentes correspondientes de Spring Security.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorResponseMapper errorResponseMapper;

    /**
     * Maneja recursos que no existen.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Recurso no encontrado | path={} | method={} | code={} | message={}",
                request.getRequestURI(),
                request.getMethod(),
                exception.getErrorCode(),
                exception.getMessage()
        );

        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getErrorCode(),
                exception.getMessage(),
                request
        );
    }

    /**
     * Maneja errores de negocio.
     */
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

    /**
     * Maneja operaciones que no son válidas según las reglas del sistema.
     */
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<StandardErrorResponse> handleInvalidOperation(
            InvalidOperationException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Operación inválida | path={} | method={} | code={} | message={}",
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

    /**
     * Maneja recursos que ya existen.
     */
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<StandardErrorResponse> handleResourceAlreadyExists(
            ResourceAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Recurso ya existente | path={} | method={} | code={} | message={}",
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

    /**
     * Maneja errores de Bean Validation.
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
     * Maneja parámetros que no pueden convertirse al tipo esperado,
     * por ejemplo, un UUID con formato inválido.
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
     * con los tipos esperados.
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
     * Maneja violaciones de integridad de la base de datos.
     *
     * No presupone que todas sean conflictos por duplicidad.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<StandardErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        log.error(
                "Violación de integridad de datos | path={} | method={}",
                request.getRequestURI(),
                request.getMethod(),
                exception
        );

        return buildResponse(
                HttpStatus.CONFLICT,
                ErrorCode.INVALID_OPERATION,
                "La operación no puede completarse debido a una restricción "
                        + "de integridad de los datos.",
                request
        );
    }

    /**
     * Maneja conflictos de concurrencia optimista.
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<StandardErrorResponse> handleOptimisticLockingFailure(
            OptimisticLockingFailureException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Conflicto de concurrencia | path={} | method={}",
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
     * Maneja fallos de transacción.
     */
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<StandardErrorResponse> handleTransactionException(
            TransactionSystemException exception,
            HttpServletRequest request
    ) {
        log.error(
                "Error de transacción | path={} | method={}",
                request.getRequestURI(),
                request.getMethod(),
                exception
        );

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR,
                "No fue posible completar la operación debido a un error "
                        + "en la transacción.",
                request
        );
    }

    /**
     * Fallback para excepciones no contempladas.
     * Evita exponer detalles internos al consumidor de la API.
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
     * Construye una respuesta estandarizada.
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
