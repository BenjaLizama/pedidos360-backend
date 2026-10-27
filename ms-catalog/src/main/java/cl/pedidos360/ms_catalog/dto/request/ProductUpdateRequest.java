package cl.pedidos360.ms_catalog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Datos permitidos para actualizar la información descriptiva
 * de un producto existente.
 *
 * <p>El SKU, precio, stock y estado no forman parte de esta operación.
 * Cada uno dispone de operaciones específicas para mantener separadas
 * las responsabilidades y reglas de negocio.</p>
 *
 * @param name nuevo nombre del producto
 * @param description nueva descripción del producto
 * @param categoryId identificador de la nueva categoría asociada
 */
public record ProductUpdateRequest(

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres.")
        String name,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres.")
        String description,

        @NotNull(message = "La categoría es obligatoria.")
        UUID categoryId
) {
}
