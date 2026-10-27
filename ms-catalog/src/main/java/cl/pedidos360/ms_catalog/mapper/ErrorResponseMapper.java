package cl.pedidos360.ms_catalog.mapper;

import cl.pedidos360.ms_catalog.dto.response.StandardErrorResponse;
import cl.pedidos360.ms_catalog.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Mapper encargado de construir las respuestas estandarizadas de error
 * utilizadas por el microservicio.
 *
 * <p>Centraliza la transformación de errores de aplicación y validación
 * hacia {@link StandardErrorResponse}, manteniendo un formato uniforme
 * para las respuestas HTTP de la API.</p>
 *
 * <p>La clase no contiene lógica de negocio. Su responsabilidad se limita
 * a transformar la información disponible en una estructura de respuesta
 * adecuada para el consumidor de la API.</p>
 */
@Component
public class ErrorResponseMapper {

    /**
     * Construye una respuesta estándar para un error controlado
     * de la aplicación.
     *
     * @param status estado HTTP asociado al error
     * @param errorCode código funcional que identifica el tipo de error
     * @param message mensaje descriptivo destinado al consumidor de la API
     * @param request solicitud HTTP que originó el error
     * @return respuesta estandarizada con la información del error
     */
    public StandardErrorResponse toResponse(
            HttpStatus status,
            ErrorCode errorCode,
            String message,
            HttpServletRequest request
    ) {
        return StandardErrorResponse.builder()
                .status(status.value())
                .code(errorCode.name())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * Construye una respuesta estándar para errores de validación
     * producidos por datos inválidos en una solicitud.
     *
     * <p>Los errores específicos de cada campo se incluyen en
     * {@code validationError} para permitir al consumidor identificar
     * qué información debe corregir.</p>
     *
     * @param validationErrors errores de validación asociados a cada campo
     * @param request solicitud HTTP que originó el error
     * @return respuesta estandarizada con estado HTTP 400 Bad Request
     */
    public StandardErrorResponse toValidationResponse(
            Map<String, String> validationErrors,
            HttpServletRequest request
    ) {
        return StandardErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.VALIDATION_ERROR.name())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("La solicitud contiene datos inválidos.")
                .path(request.getRequestURI())
                .timestamp(System.currentTimeMillis())
                .validationError(validationErrors)
                .build();
    }

    /**
     * Construye una respuesta estándar para errores internos no controlados.
     *
     * <p>El mensaje general entregado al cliente evita exponer detalles
     * internos de la aplicación. El mensaje de la excepción se conserva
     * en {@code developerMessage} para facilitar el diagnóstico.</p>
     *
     * @param exception excepción que originó el error interno
     * @param request solicitud HTTP que originó el error
     * @return respuesta estandarizada con estado HTTP 500 Internal Server Error
     */
    public StandardErrorResponse toInternalServerError(
            Exception exception,
            HttpServletRequest request
    ) {
        return StandardErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code(ErrorCode.INTERNAL_SERVER_ERROR.name())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("Ha ocurrido un error interno en el servidor.")
                .developerMessage(exception.getMessage())
                .path(request.getRequestURI())
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
