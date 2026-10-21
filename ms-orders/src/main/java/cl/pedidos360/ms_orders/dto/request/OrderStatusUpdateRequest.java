package cl.pedidos360.ms_orders.dto.request;

import cl.pedidos360.ms_orders.enums.OrderStateEnum;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(

        @NotNull(message = "El nuevo estado de la orden es obligatorio")
        OrderStateEnum status

) {
}
