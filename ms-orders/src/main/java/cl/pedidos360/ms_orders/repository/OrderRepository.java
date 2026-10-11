package cl.pedidos360.ms_orders.repository;

import cl.pedidos360.ms_orders.entity.OrderEntity;
import cl.pedidos360.ms_orders.enums.OrderStateEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la persistencia y consulta de órdenes.
 */
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    /**
     * Consulta las órdenes de un cliente de forma paginada.
     */
    Page<OrderEntity> findAllByClientId(
            UUID clientId,
            Pageable pageable
    );

    /**
     * Busca una orden perteneciente a un cliente específico.
     */
    Optional<OrderEntity> findByIdAndClientId(
            UUID id,
            UUID clientId
    );

    /**
     * Consulta las órdenes de un cliente según su estado.
     */
    Page<OrderEntity> findAllByClientIdAndOrderState(
            UUID clientId,
            OrderStateEnum orderState,
            Pageable pageable
    );

    /**
     * Consulta las órdenes por estado.
     */
    Page<OrderEntity> findAllByOrderState(
            OrderStateEnum orderState,
            Pageable pageable
    );
}
