
package cl.pedidos360.ms_orders.service;

import cl.pedidos360.ms_orders.dto.request.OrderCreateRequest;
import cl.pedidos360.ms_orders.dto.response.OrderResponse;
import cl.pedidos360.ms_orders.enums.OrderStateEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {

    OrderResponse create(OrderCreateRequest request);

    Page<OrderResponse> findAll(
            UUID customerId,
            OrderStateEnum status,
            Pageable pageable
    );

    OrderResponse findById(UUID orderId);

    Page<OrderResponse> findMyOrders(Pageable pageable);

    OrderResponse cancel(UUID orderId);

    OrderResponse updateStatus(UUID orderId, OrderStateEnum newStatus);
}
