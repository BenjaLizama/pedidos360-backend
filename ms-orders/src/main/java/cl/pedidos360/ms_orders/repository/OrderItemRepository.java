package cl.pedidos360.ms_orders.repository;

import cl.pedidos360.ms_orders.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio para la persistencia y consulta de los ítems de una orden.
 */
public interface OrderItemRepository
        extends JpaRepository<OrderItemEntity, UUID> {

    /**
     * Obtiene los ítems asociados a una orden.
     */
    List<OrderItemEntity> findAllByOrder_Id(UUID orderId);

    /**
     * Obtiene los ítems asociados a un producto específico.
     */
    List<OrderItemEntity> findAllByProductId(UUID productId);
}
