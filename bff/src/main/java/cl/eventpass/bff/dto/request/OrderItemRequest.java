package cl.eventpass.bff.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO de solicitud que representa un producto incluido en una orden de compra.
 *
 * <p>Define el identificador del producto y la cantidad solicitada.
 * Las validaciones aseguran que ambos campos sean obligatorios y que
 * la cantidad sea igual o superior a uno.</p>
 *
 * @param productId identificador único del producto que se desea comprar.
 * @param quantity cantidad de unidades solicitadas del producto.
 */
public record OrderItemRequest(

        /**
         * Identificador único del producto.
         *
         * <p>Este campo es obligatorio para identificar el producto
         * que se incluirá en la orden.</p>
         */
        @NotNull(message = "El ID del producto es obligatorio")
        UUID productId,

        /**
         * Cantidad de unidades solicitadas.
         *
         * <p>Este campo es obligatorio y debe ser como mínimo uno.</p>
         */
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser como mínimo 1")
        Integer quantity

) {
}
