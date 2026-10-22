package cl.pedidos360.ms_orders.repository;

import cl.pedidos360.ms_orders.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio encargado de la persistencia y consulta de los ítems de una orden.
 *
 * <p>Extiende {@link JpaRepository} para acceder a las operaciones CRUD
 * proporcionadas por Spring Data JPA y definir consultas mediante
 * convenciones en los nombres de los métodos.</p>
 */
public interface OrderItemRepository
        extends JpaRepository<OrderItemEntity, UUID> {

    /**
     * Obtiene todos los ítems asociados a una orden específica.
     *
     * <p>La consulta se realiza utilizando el identificador de la orden
     * relacionada con cada ítem.</p>
     *
     * @param orderId identificador único de la orden.
     * @return lista de ítems asociados a la orden indicada.
     */
    List<OrderItemEntity> findAllByOrder_Id(UUID orderId);

    /**
     * Obtiene todos los ítems asociados a un producto específico.
     *
     * @param productId identificador único del producto.
     * @return lista de ítems que corresponden al producto indicado.
     */
    List<OrderItemEntity> findAllByProductId(UUID productId);
}
