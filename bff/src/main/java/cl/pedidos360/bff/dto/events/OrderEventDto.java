package cl.pedidos360.bff.dto.events;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderEventDto(
        UUID orderId,
        BigDecimal amount
) {
}
