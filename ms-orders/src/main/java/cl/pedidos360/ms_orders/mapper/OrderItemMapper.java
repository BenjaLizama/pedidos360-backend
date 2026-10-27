package cl.pedidos360.ms_orders.mapper;

import cl.pedidos360.ms_orders.dto.response.OrderItemResponse;
import cl.pedidos360.ms_orders.entity.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

/**
 * Mapper encargado de transformar entidades de detalle de pedidos
 * en objetos de respuesta.
 *
 * <p>Utiliza MapStruct para generar automáticamente la implementación
 * de la conversión entre {@link OrderItemEntity} y {@link OrderItemResponse}.</p>
 *
 * <p>Además, calcula el subtotal de cada artículo a partir de su precio
 * unitario y cantidad.</p>
 */
@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    /**
     * Convierte una entidad de detalle de pedido en su DTO de respuesta.
     *
     * <p>El campo {@code subtotal} se calcula mediante el método
     * {@link #calculateSubtotal(OrderItemEntity)}.</p>
     *
     * @param entity entidad del detalle de pedido que se desea convertir.
     * @return DTO con la información del detalle del pedido y su subtotal.
     */
    @Mapping(target = "subtotal", expression = "java(calculateSubtotal(entity))")
    OrderItemResponse toResponse(OrderItemEntity entity);

    /**
     * Calcula el subtotal de un artículo multiplicando su precio unitario
     * por la cantidad solicitada.
     *
     * <p>Si el precio unitario o la cantidad son nulos, devuelve
     * {@link BigDecimal#ZERO}.</p>
     *
     * @param entity entidad del detalle de pedido utilizada para el cálculo.
     * @return subtotal calculado o cero si falta el precio o la cantidad.
     */
    default BigDecimal calculateSubtotal(OrderItemEntity entity) {
        if (entity.getUnitPrice() == null || entity.getQuantity() == null) {
            return BigDecimal.ZERO;
        }

        return entity.getUnitPrice()
                .multiply(BigDecimal.valueOf(entity.getQuantity()));
    }
}
