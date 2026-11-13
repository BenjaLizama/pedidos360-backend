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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.TreeMap;

/**
 * Manejador global de excepciones del microservicio ms-orders.
 *
 * <p>Centraliza el tratamiento de errores producidos durante la ejecución
 * de las solicitudes HTTP, incluyendo errores de negocio, validaciones,
 * operaciones de persistencia y excepciones inesperadas.</p>
 *
 * <p>Utiliza {@link RestControllerAdvice} para interceptar excepciones
 * generadas por los controladores y construir respuestas HTTP consistentes
 * mediante {@link StandardErrorResponse}.</p>
 *
 * <p>La construcción de las respuestas se delega a {@link ErrorResponseMapper},
 * evitando duplicar la lógica de transformación de errores.</p>
 *
 * <p>Las excepciones de autenticación y autorización se gestionan mediante
 * los componentes de seguridad configurados en Spring Security.</p>
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    /**
     * Componente encargado de transformar las excepciones y los errores
     * en objetos de respuesta estandarizados.
     */
    private final ErrorResponseMapper errorResponseMapper;

    /**
     * Maneja las excepciones correspondientes a recursos inexistentes.
     *
     * <p>Registra la información de la solicitud y devuelve una respuesta
     * HTTP 404 (Not Found) con el código y el mensaje de la excepción.</p>
     *
     * @param exception excepción que indica que el recurso solicitado no existe.
     * @param request solicitud HTTP en la que se produjo el error.
     * @return respuesta estandarizada con estado HTTP 404.
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
     * Maneja las excepciones correspondientes a errores de negocio.
     *
     * <p>Registra el error y devuelve una respuesta HTTP 409 (Conflict),
     * utilizando el código de error y el mensaje definidos en la excepción.</p>
     *
     * @param exception excepción que representa un error de negocio.
     * @param request solicitud HTTP en la que se produjo el error.
     * @return respuesta estandarizada con estado HTTP 409.
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
     * Maneja las excepciones producidas por operaciones que incumplen
     * las reglas de negocio o las restricciones funcionales del sistema.
     *
     * <p>Devuelve una respuesta HTTP 409 (Conflict) con el código de error
     * y el mensaje asociados a la excepción.</p>
     *
     * @param exception excepción que representa una operación inválida.
     * @param request solicitud HTTP en la que se produjo el error.
     * @return respuesta estandarizada con estado HTTP 409.
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
     * Maneja las excepciones que indican que un recurso ya existe.
     *
     * <p>Devuelve una respuesta HTTP 409 (Conflict) para indicar que
     * la operación solicitada entra en conflicto con un recurso existente.</p>
     *
     * @param exception excepción que indica la existencia previa del recurso.
     * @param request solicitud HTTP en la que se produjo el error.
     * @return respuesta estandarizada con estado HTTP 409.
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
     * Maneja los errores de validación generados por Bean Validation.
     *
     * <p>Recopila los errores asociados a los campos de la solicitud,
     * organiza sus nombres mediante un {@link TreeMap} y conserva el primer
     * mensaje registrado para cada campo.</p>
     *
     * <p>Delega la creación de la respuesta al mapper y devuelve el estado
     * HTTP 400 (Bad Request).</p>
     *
     * @param exception excepción que contiene los errores de validación.
     * @param request solicitud HTTP en la que se produjo el error.
     * @return respuesta estandarizada con los errores de validación
     *         y estado HTTP 400.
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
     * Maneja los errores de conversión de parámetros de una solicitud HTTP.
     *
     * <p>Se utiliza, por ejemplo, cuando un parámetro debe convertirse
     * a UUID, pero su valor no tiene el formato esperado.</p>
     *
     * @param exception excepción que contiene el nombre del parámetro inválido.
     * @param request solicitud HTTP en la que se produjo el error.
     * @return respuesta estandarizada con estado HTTP 400.
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
     * Maneja los errores de lectura o deserialización del cuerpo de una solicitud.
     *
     * <p>Se aplica cuando el cuerpo de la petición está vacío, contiene JSON
     * mal formado o presenta datos que no pueden convertirse a los tipos
     * esperados por la aplicación.</p>
     *
     * @param exception excepción producida al leer el cuerpo de la solicitud.
     * @param request solicitud HTTP en la que se produjo el error.
     * @return respuesta estandarizada con estado HTTP 400.
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
     * Maneja las violaciones de restricciones de integridad de la base de datos.
     *
     * <p>Registra la excepción completa para facilitar el diagnóstico y devuelve
     * un mensaje genérico al consumidor de la API, sin asumir que el problema
     * corresponde necesariamente a una duplicidad de datos.</p>
     *
     * @param exception excepción producida por una violación de integridad.
     * @param request solicitud HTTP en la que se produjo el error.
     * @return respuesta estandarizada con estado HTTP 409.
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
     * Maneja los conflictos detectados mediante control de concurrencia optimista.
     *
     * <p>Informa al consumidor que el recurso pudo haber sido modificado
     * por otro proceso y que debe volver a consultarlo antes de reintentar
     * la operación.</p>
     *
     * @param exception excepción que representa un conflicto de concurrencia.
     * @param request solicitud HTTP en la que se produjo el error.
     * @return respuesta estandarizada con estado HTTP 409.
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
     * Maneja los errores producidos durante la ejecución o finalización
     * de una transacción.
     *
     * <p>Registra la excepción para facilitar el diagnóstico y devuelve
     * un mensaje genérico con estado HTTP 500, evitando exponer detalles
     * internos de la transacción al consumidor.</p>
     *
     * @param exception excepción relacionada con la transacción.
     * @param request solicitud HTTP en la que se produjo el error.
     * @return respuesta estandarizada con estado HTTP 500.
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
     * Maneja los errores de autorización producidos por reglas de acceso
     * a nivel de método o de servicio.
     *
     * <p>Devuelve una respuesta HTTP 403 (Forbidden) en lugar de tratarlos
     * como errores internos.</p>
     *
     * @param exception excepción que indica acceso denegado.
     * @param request solicitud HTTP en la que se produjo el error.
     * @return respuesta estandarizada con estado HTTP 403.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<StandardErrorResponse> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return accessDeniedResponse(exception, request);
    }

    /**
     * Maneja las denegaciones emitidas por la autorización de métodos
     * (por ejemplo, las reglas {@code @PreAuthorize} de Spring Security 7).
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<StandardErrorResponse> handleAuthorizationDenied(
            AuthorizationDeniedException exception,
            HttpServletRequest request
    ) {
        return accessDeniedResponse(exception, request);
    }

    private ResponseEntity<StandardErrorResponse> accessDeniedResponse(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Acceso denegado | path={} | method={} | message={}",
                request.getRequestURI(),
                request.getMethod(),
                exception.getMessage()
        );

        return buildResponse(
                HttpStatus.FORBIDDEN,
                ErrorCode.ACCESS_DENIED,
                "No tiene permiso para acceder a este recurso.",
                request
        );
    }

    /**
     * Maneja las excepciones no contempladas por los demás métodos del handler.
     *
     * <p>Actúa como mecanismo de respaldo para errores inesperados,
     * registra la excepción completa y delega la creación de la respuesta
     * interna al {@link ErrorResponseMapper}.</p>
     *
     * @param exception excepción inesperada producida durante la solicitud.
     * @param request solicitud HTTP en la que se produjo el error.
     * @return respuesta estandarizada con estado HTTP 500.
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
     * Construye una respuesta HTTP utilizando el formato estandarizado de errores.
     *
     * <p>Delega al mapper la creación del cuerpo de respuesta y establece
     * el código HTTP proporcionado.</p>
     *
     * @param status estado HTTP que se devolverá al cliente.
     * @param errorCode código de error asociado al problema.
     * @param message mensaje descriptivo del error.
     * @param request solicitud HTTP en la que se produjo el error.
     * @return respuesta HTTP con su estado y cuerpo estandarizado.
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
