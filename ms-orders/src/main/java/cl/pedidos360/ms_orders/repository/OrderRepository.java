package cl.pedidos360.ms_orders.repository;

import cl.pedidos360.ms_orders.entity.OrderEntity;
import cl.pedidos360.ms_orders.enums.OrderStateEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio encargado de la persistencia y consulta de órdenes.
 *
 * <p>Extiende {@link JpaRepository} para disponer de las operaciones CRUD
 * de Spring Data JPA y definir consultas personalizadas mediante los nombres
 * de sus métodos.</p>
 *
 * <p>Permite consultar órdenes por cliente, identificador y estado,
 * incluyendo resultados paginados.</p>
 */
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    /**
     * Consulta las órdenes pertenecientes a un cliente de forma paginada.
     *
     * @param clientId identificador único del cliente.
     * @param pageable configuración de paginación y ordenamiento.
     * @return página con las órdenes del cliente.
     */
    Page<OrderEntity> findAllByClientId(
            UUID clientId,
            Pageable pageable
    );

    /**
     * Busca una orden por su identificador y el identificador de su cliente.
     *
     * <p>Permite verificar que la orden corresponda al cliente indicado.</p>
     *
     * @param id identificador único de la orden.
     * @param clientId identificador único del cliente propietario de la orden.
     * @return un {@link Optional} con la orden encontrada, o vacío si no existe
     *         una coincidencia para ambos identificadores.
     */
    Optional<OrderEntity> findByIdAndClientId(
            UUID id,
            UUID clientId
    );

    /**
     * Consulta las órdenes de un cliente filtradas por su estado.
     *
     * @param clientId identificador único del cliente.
     * @param orderState estado por el que se desea filtrar las órdenes.
     * @param pageable configuración de paginación y ordenamiento.
     * @return página con las órdenes del cliente que coinciden con el estado.
     */
    Page<OrderEntity> findAllByClientIdAndOrderState(
            UUID clientId,
            OrderStateEnum orderState,
            Pageable pageable
    );

    /**
     * Consulta las órdenes que coinciden con un estado específico.
     *
     * @param orderState estado por el que se desea filtrar las órdenes.
     * @param pageable configuración de paginación y ordenamiento.
     * @return página con las órdenes que coinciden con el estado indicado.
     */
    Page<OrderEntity> findAllByOrderState(
            OrderStateEnum orderState,
            Pageable pageable
    );

    @Query("""
        SELECT o.orderState AS orderState, COUNT(o) AS total
        FROM OrderEntity o
        GROUP BY o.orderState
    """)
    List<OrderStatusCountProjection> countOrdersByStatus();
}
