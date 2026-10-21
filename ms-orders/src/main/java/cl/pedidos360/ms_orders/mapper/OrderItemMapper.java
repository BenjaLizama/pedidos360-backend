package cl.pedidos360.ms_orders.mapper;

import cl.pedidos360.ms_orders.dto.response.OrderItemResponse;
import cl.pedidos360.ms_orders.entity.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "subtotal", expression = "java(calculateSubtotal(entity))")
    OrderItemResponse toResponse(OrderItemEntity entity);

    default BigDecimal calculateSubtotal(OrderItemEntity entity) {
        if (entity.getUnitPrice() == null || entity.getQuantity() == null) {
            return BigDecimal.ZERO;
        }

        return entity.getUnitPrice()
                .multiply(BigDecimal.valueOf(entity.getQuantity()));
    }
}
