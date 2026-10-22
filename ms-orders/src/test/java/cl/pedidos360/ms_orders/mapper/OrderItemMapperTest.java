package cl.pedidos360.ms_orders.mapper;

import cl.pedidos360.ms_orders.dto.response.OrderItemResponse;
import cl.pedidos360.ms_orders.entity.OrderItemEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class OrderItemMapperTest {

    private final OrderItemMapper mapper =
            Mappers.getMapper(OrderItemMapper.class);

    @Test
    void calculateSubtotal_shouldMultiplyUnitPriceByQuantity() {
        OrderItemEntity entity = OrderItemEntity.builder()
                .productId(UUID.randomUUID())
                .quantity(3)
                .unitPrice(new BigDecimal("1500.00"))
                .build();

        BigDecimal subtotal = mapper.calculateSubtotal(entity);

        assertEquals(new BigDecimal("4500.00"), subtotal);
    }

    @Test
    void calculateSubtotal_shouldReturnZeroWhenUnitPriceIsNull() {
        OrderItemEntity entity = OrderItemEntity.builder()
                .productId(UUID.randomUUID())
                .quantity(2)
                .unitPrice(null)
                .build();

        BigDecimal subtotal = mapper.calculateSubtotal(entity);

        assertEquals(BigDecimal.ZERO, subtotal);
    }

    @Test
    void calculateSubtotal_shouldReturnZeroWhenQuantityIsNull() {
        OrderItemEntity entity = OrderItemEntity.builder()
                .productId(UUID.randomUUID())
                .quantity(null)
                .unitPrice(new BigDecimal("1500.00"))
                .build();

        BigDecimal subtotal = mapper.calculateSubtotal(entity);

        assertEquals(BigDecimal.ZERO, subtotal);
    }

    @Test
    void toResponse_shouldMapFieldsAndCalculateSubtotal() {
        UUID productId = UUID.randomUUID();

        OrderItemEntity entity = OrderItemEntity.builder()
                .productId(productId)
                .quantity(4)
                .unitPrice(new BigDecimal("250.00"))
                .build();

        OrderItemResponse response = mapper.toResponse(entity);

        assertNotNull(response);
        assertEquals(productId, response.productId());
        assertEquals(4, response.quantity());
        assertEquals(new BigDecimal("250.00"), response.unitPrice());
        assertEquals(new BigDecimal("1000.00"), response.subtotal());
    }
}
