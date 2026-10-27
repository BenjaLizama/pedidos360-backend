package cl.pedidos360.ms_orders.service;

import cl.pedidos360.ms_orders.dto.request.OrderCreateRequest;
import cl.pedidos360.ms_orders.dto.response.DashboardSummaryResponse;
import cl.pedidos360.ms_orders.dto.response.OrderResponse;
import cl.pedidos360.ms_orders.enums.OrderStateEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Define las operaciones de negocio disponibles para la gestión de órdenes.
 *
 * <p>Establece el contrato que debe implementar el servicio encargado
 * de crear, consultar, cancelar y actualizar el estado de las órdenes.</p>
 *
 * <p>Las consultas que devuelven múltiples órdenes utilizan paginación
 * mediante {@link Pageable} y retornan los resultados como {@link Page}.</p>
 */
public interface OrderService {

    /**
     * Crea una nueva orden para el usuario autenticado.
     *
     * @param request datos necesarios para crear la orden y sus ítems.
     * @return información de la orden creada.
     */
    OrderResponse create(OrderCreateRequest request);

    /**
     * Consulta las órdenes aplicando filtros opcionales por cliente y estado.
     *
     * @param customerId identificador del cliente por el que se desea filtrar,
     *                   o {@code null} para no aplicar este filtro.
     * @param status estado por el que se desea filtrar,
     *               o {@code null} para no aplicar este filtro.
     * @param pageable configuración de paginación y ordenamiento.
     * @return página de órdenes que cumplen los filtros especificados.
     */
    Page<OrderResponse> findAll(
            UUID customerId,
            OrderStateEnum status,
            Pageable pageable
    );

    /**
     * Obtiene una orden mediante su identificador.
     *
     * @param orderId identificador único de la orden.
     * @return información de la orden encontrada.
     */
    OrderResponse findById(UUID orderId);

    /**
     * Consulta las órdenes pertenecientes al usuario autenticado.
     *
     * @param pageable configuración de paginación y ordenamiento.
     * @return página con las órdenes del usuario autenticado.
     */
    Page<OrderResponse> findMyOrders(Pageable pageable);

    /**
     * Cancela una orden perteneciente al usuario autenticado.
     *
     * @param orderId identificador único de la orden que se desea cancelar.
     * @return información de la orden después de la cancelación.
     */
    OrderResponse cancel(UUID orderId);

    /**
     * Actualiza el estado de una orden, validando que la transición solicitada
     * esté permitida.
     *
     * @param orderId identificador único de la orden que se desea actualizar.
     * @param newStatus nuevo estado que se desea asignar a la orden.
     * @return información de la orden con el estado actualizado.
     */
    OrderResponse updateStatus(UUID orderId, OrderStateEnum newStatus);


    /**
     * Acepta una orden en estado CREADO y solicita la reducción de stock.
     *
     * @param orderId identificador único de la orden.
     * @return orden aceptada.
     */
    OrderResponse accept(UUID orderId);

    DashboardSummaryResponse getDashboardSummary();
}
