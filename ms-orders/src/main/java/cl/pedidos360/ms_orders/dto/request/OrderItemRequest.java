package cl.pedidos360.ms_orders.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderItemRequest(
        @NotNull(message = "El ID del producto es obligatorio")
        UUID productId,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser como mínimo 1")
        Integer quantity
) {
}
