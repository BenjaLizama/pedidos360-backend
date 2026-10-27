package cl.pedidos360.ms_catalog.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import org.springframework.http.HttpStatus;

/**
 * Estructura estándar utilizada para las respuestas exitosas de la API.
 *
 * <p>Permite mantener un formato uniforme para las operaciones que
 * devuelven información al consumidor del microservicio.</p>
 *
 * <p>El campo {@code data} es genérico para permitir que la misma
 * estructura sea utilizada con diferentes tipos de respuesta.</p>
 *
 * @param <T> tipo de información contenida en la respuesta
 * @param status código HTTP asociado a la operación
 * @param message mensaje descriptivo de la operación realizada
 * @param data información resultante de la operación
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StandardResponse<T>(
        Integer status,
        String message,
        T data
) {

    /**
     * Crea una respuesta correspondiente a una operación de creación exitosa.
     *
     * @param message mensaje descriptivo de la operación
     * @param data información del recurso creado
     * @param <T> tipo de información contenida en la respuesta
     * @return respuesta con estado HTTP 201 Created
     */
    public static <T> StandardResponse<T> created(String message, T data) {
        return new StandardResponse<>(
                HttpStatus.CREATED.value(),
                message,
                data
        );
    }

    /**
     * Crea una respuesta correspondiente a una operación exitosa.
     *
     * @param message mensaje descriptivo de la operación
     * @param data información resultante de la operación
     * @param <T> tipo de información contenida en la respuesta
     * @return respuesta con estado HTTP 200 OK
     */
    public static <T> StandardResponse<T> ok(String message, T data) {
        return new StandardResponse<>(
                HttpStatus.OK.value(),
                message,
                data
        );
    }
}
