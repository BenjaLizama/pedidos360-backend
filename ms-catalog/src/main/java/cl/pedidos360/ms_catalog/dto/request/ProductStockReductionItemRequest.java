package cl.pedidos360.ms_catalog.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

/**
 * Representa un producto y la cantidad de unidades que se deben
 * descontar de sus existencias.
 *
 * <p>Este DTO se utiliza como elemento de la solicitud para la
 * reducción masiva de stock.</p>
 *
 * @param productId identificador UUID del producto
 * @param quantity cantidad de unidades que se descontarán
 */
public record ProductStockReductionItemRequest(

        @NotNull(message = "El identificador del producto es obligatorio.")
        UUID productId,

        @NotNull(message = "La cantidad es obligatoria.")
        @Positive(message = "La cantidad debe ser mayor que cero.")
        Integer quantity
) {
}
