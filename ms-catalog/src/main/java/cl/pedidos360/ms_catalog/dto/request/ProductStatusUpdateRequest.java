package cl.pedidos360.ms_catalog.dto.request;

import cl.pedidos360.ms_catalog.enums.ProductStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Datos necesarios para modificar el estado de un producto.
 *
 * <p>El estado se actualiza mediante una operación independiente
 * para evitar mezclar cambios de ciclo de vida con modificaciones
 * de información comercial.</p>
 *
 * @param status nuevo estado que tendrá el producto
 */
public record ProductStatusUpdateRequest(

        @NotNull(message = "El estado es obligatorio.")
        ProductStatus status
) {
}
