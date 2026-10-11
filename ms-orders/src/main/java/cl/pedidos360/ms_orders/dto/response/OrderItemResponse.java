package cl.pedidos360.ms_orders.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Representa un ítem de una orden en la respuesta de la API.
 */
public record OrderItemResponse(

        UUID id,
        UUID productId,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal

) {
}
