package cl.pedidos360.ms_orders.dto.response;

import cl.pedidos360.ms_orders.enums.OrderStateEnum;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta que representa una orden de compra.
 *
 * <p>Contiene la información principal de la orden, incluyendo su identificador,
 * el cliente asociado, el estado actual, el importe total y los ítems que
 * la componen.</p>
 *
 * <p>También expone las fechas de creación y última modificación para
 * proporcionar información temporal sobre el registro.</p>
 *
 * @param id identificador único de la orden.
 * @param clientId identificador único del cliente propietario de la orden.
 * @param orderState estado actual de la orden, representado por
 *                   {@link OrderStateEnum}.
 * @param total importe total de la orden.
 * @param items lista de ítems que componen la orden.
 * @param createdAt fecha y hora de creación de la orden.
 * @param updatedAt fecha y hora de la última modificación de la orden.
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
