package cl.pedidos360.ms_orders.mapper;

import cl.pedidos360.ms_orders.dto.response.OrderResponse;
import cl.pedidos360.ms_orders.entity.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper encargado de convertir entidades de pedidos en sus DTO de respuesta.
 *
 * <p>Utiliza MapStruct para generar automáticamente la implementación
 * de la conversión entre {@link OrderEntity} y {@link OrderResponse}.</p>
 *
 * <p>Delega la conversión de los detalles del pedido en {@link OrderItemMapper},
 * permitiendo transformar la lista de entidades asociadas en una lista
 * de objetos {@code OrderItemResponse}.</p>
 */
@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {

    /**
     * Convierte una entidad de pedido en su DTO de respuesta.
     *
     * <p>Los campos compatibles se mapean automáticamente por nombre.
     * La propiedad {@code items} se mapea explícitamente y utiliza
     * {@link OrderItemMapper} para convertir cada detalle.</p>
     *
     * @param entity entidad del pedido que se desea convertir.
     * @return DTO con la información del pedido y sus detalles.
     */
    @Mapping(target = "items", source = "items")
    OrderResponse toResponse(OrderEntity entity);
}
