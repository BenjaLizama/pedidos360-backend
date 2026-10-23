package cl.pedidos360.ms_orders.mapper;

import cl.pedidos360.ms_orders.dto.response.OrderResponse;
import cl.pedidos360.ms_orders.entity.OrderEntity;
import cl.pedidos360.ms_orders.entity.OrderItemEntity;
import cl.pedidos360.ms_orders.enums.OrderStateEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        OrderMapperImpl.class,
        OrderItemMapperImpl.class
})
class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    @Test
    void toResponse_shouldMapOrderAndItsItems() {
        UUID clientId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderItemEntity item = OrderItemEntity.builder()
                .productId(productId)
                .quantity(2)
                .unitPrice(new BigDecimal("1200.00"))
                .build();

        OrderEntity entity = OrderEntity.builder()
                .clientId(clientId)
                .orderState(OrderStateEnum.CREADO)
                .total(new BigDecimal("2400.00"))
                .build();

        entity.addItem(item);

        OrderResponse response = orderMapper.toResponse(entity);

        assertNotNull(response);
        assertEquals(clientId, response.clientId());
        assertEquals(OrderStateEnum.CREADO, response.orderState());
        assertEquals(new BigDecimal("2400.00"), response.total());

        assertNotNull(response.items());
        assertEquals(1, response.items().size());

        assertEquals(productId, response.items().get(0).productId());
        assertEquals(2, response.items().get(0).quantity());
        assertEquals(new BigDecimal("1200.00"),
                response.items().get(0).unitPrice());
        assertEquals(new BigDecimal("2400.00"),
                response.items().get(0).subtotal());
    }

    @Test
    void toResponse_shouldMapOrderWithoutItems() {
        UUID clientId = UUID.randomUUID();

        OrderEntity entity = OrderEntity.builder()
                .clientId(clientId)
                .orderState(OrderStateEnum.CREADO)
                .total(BigDecimal.ZERO)
                .build();

        OrderResponse response = orderMapper.toResponse(entity);

        assertNotNull(response);
        assertEquals(clientId, response.clientId());
        assertEquals(OrderStateEnum.CREADO, response.orderState());
        assertEquals(BigDecimal.ZERO, response.total());
        assertNotNull(response.items());
        assertEquals(0, response.items().size());
    }
}
