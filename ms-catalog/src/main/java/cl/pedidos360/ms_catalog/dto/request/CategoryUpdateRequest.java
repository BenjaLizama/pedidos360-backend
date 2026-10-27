package cl.pedidos360.ms_catalog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos permitidos para actualizar la información descriptiva
 * de una categoría existente.
 *
 * <p>El estado se modifica mediante {@link CategoryStatusUpdateRequest}
 * y no forma parte de esta operación.</p>
 *
 * @param name nuevo nombre de la categoría
 * @param description nueva descripción de la categoría
 */
public record CategoryUpdateRequest(

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.")
        String name,

        @Size(max = 300, message = "La descripción no puede superar los 300 caracteres.")
        String description
) {
}
