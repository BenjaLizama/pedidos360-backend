package cl.pedidos360.ms_orders.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderCreateRequest(
        @NotEmpty(message = "La orden debe contener al menos un producto")
        List<@Valid OrderItemRequest> items
) {
}
