package cl.pedidos360.ms_orders.dto.response;

import cl.pedidos360.ms_orders.enums.OrderStateEnum;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Representa una orden en la respuesta de la API.
 */
public record OrderResponse(

        UUID id,
        UUID clientId,
        OrderStateEnum orderState,
        BigDecimal total,
        List<OrderItemResponse> items,
        Instant createdAt,
        Instant updatedAt

) {
}
