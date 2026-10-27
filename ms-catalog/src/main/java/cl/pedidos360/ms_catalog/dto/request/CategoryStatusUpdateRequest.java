package cl.pedidos360.ms_catalog.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Datos necesarios para modificar el estado de una categoría.
 *
 * <p>La actualización del estado se mantiene como una operación
 * independiente de la modificación de los datos descriptivos.</p>
 *
 * @param active indica si la categoría debe quedar activa
 */
public record CategoryStatusUpdateRequest(

        @NotNull(message = "El estado es obligatorio.")
        Boolean active
) {
}
