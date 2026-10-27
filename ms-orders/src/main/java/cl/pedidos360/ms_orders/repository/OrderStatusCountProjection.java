package cl.pedidos360.ms_orders.repository;

import cl.pedidos360.ms_orders.enums.OrderStateEnum;

public interface OrderStatusCountProjection {

    OrderStateEnum getOrderState();

    Long getTotal();
}
