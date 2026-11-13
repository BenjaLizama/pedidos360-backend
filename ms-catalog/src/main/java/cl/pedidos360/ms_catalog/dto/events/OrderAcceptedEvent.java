package cl.pedidos360.ms_catalog.dto.events;

import java.util.List;
import java.util.UUID;

public record OrderAcceptedEvent(
        UUID eventId,
        UUID orderId,
        List<OrderItemEvent> items
) {
    public record OrderItemEvent(
            UUID productId,
            int quantity
    ) {}
}