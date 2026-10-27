package cl.pedidos360.ms_orders.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.Map;

/**
 * Estructura estándar utilizada para representar errores de la API.
 *
 * <p>Centraliza la información entregada al cliente cuando ocurre un error,
 * permitiendo mantener un formato consistente entre los diferentes
 * endpoints del microservicio.</p>
 *
 * <p>Los campos opcionales se omiten de la respuesta JSON cuando su valor
 * es {@code null}, evitando exponer información innecesaria.</p>
 *
 * @param status código HTTP asociado al error
 * @param code código funcional interno del error
 * @param error descripción general del estado HTTP
 * @param message mensaje destinado al consumidor de la API
 * @param developerMessage información adicional orientada al diagnóstico
 * @param path ruta del endpoint que originó el error
 * @param timestamp instante en que se generó la respuesta
 * @param validationError errores específicos asociados a validaciones
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StandardErrorResponse(
        Integer status,
        String code,
        String error,
        String message,
        String developerMessage,
        String path,
        Long timestamp,
        Map<String, String> validationError
) {
}
