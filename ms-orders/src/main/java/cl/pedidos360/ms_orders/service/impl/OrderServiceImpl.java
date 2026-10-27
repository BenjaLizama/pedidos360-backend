package cl.pedidos360.ms_orders.service.impl;

import cl.pedidos360.ms_orders.client.CatalogClient;
import cl.pedidos360.ms_orders.dto.events.OrderCreatedEvent;
import cl.pedidos360.ms_orders.dto.request.BulkStockReductionItemRequest;
import cl.pedidos360.ms_orders.dto.request.OrderCreateRequest;
import cl.pedidos360.ms_orders.dto.request.OrderItemRequest;
import cl.pedidos360.ms_orders.dto.response.DashboardSummaryResponse;
import cl.pedidos360.ms_orders.dto.response.OrderResponse;
import cl.pedidos360.ms_orders.entity.OrderEntity;
import cl.pedidos360.ms_orders.entity.OrderItemEntity;
import cl.pedidos360.ms_orders.enums.OrderStateEnum;
import cl.pedidos360.ms_orders.exception.InvalidOperationException;
import cl.pedidos360.ms_orders.exception.ResourceNotFoundException;
import cl.pedidos360.ms_orders.mapper.OrderMapper;
import cl.pedidos360.ms_orders.repository.OrderRepository;
import cl.pedidos360.ms_orders.repository.OrderStatusCountProjection;
import cl.pedidos360.ms_orders.security.CurrentUserProvider;
import cl.pedidos360.ms_orders.service.OrderPublisherService;
import cl.pedidos360.ms_orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.*;

/**
 * Implementa las operaciones de negocio para la gestión de órdenes.
 *
 * <p>Coordina la creación, consulta, cancelación y actualización del estado
 * de las órdenes, interactuando con el repositorio, los mappers, el proveedor
 * de identidad del usuario autenticado y el cliente HTTP de ms-catalog.</p>
 *
 * <p>Durante la creación de una orden, consulta el catálogo para obtener
 * los precios de los productos y calcula los subtotales y el total.
 * También verifica los permisos de acceso para las operaciones administrativas
 * y valida las transiciones permitidas entre estados.</p>
 *
 * <p>Las operaciones son de solo lectura por defecto. Los métodos que
 * modifican información definen explícitamente sus propias transacciones
 * de escritura.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    /**
     * Repositorio encargado de persistir y consultar las órdenes.
     */
    private final OrderRepository orderRepository;

    /**
     * Mapper utilizado para convertir entidades de órdenes en DTO de respuesta.
     */
    private final OrderMapper orderMapper;

    /**
     * Proveedor que obtiene el identificador del usuario autenticado.
     */
    private final CurrentUserProvider currentUserProvider;

    /**
     * Cliente HTTP utilizado para consultar los productos de ms-catalog.
     */
    private final CatalogClient catalogClient;


    private final OrderPublisherService orderPublisherService;

    /**
     * Crea una orden para el usuario autenticado.
     *
     * <p>Consulta el precio de cada producto en ms-catalog, calcula el subtotal
     * de cada ítem y el total de la orden, y finalmente persiste la entidad.
     * La orden comienza en estado {@link OrderStateEnum#CREADO}.</p>
     *
     * @param request datos de la orden y los ítems que la componen.
     * @return representación de la orden creada.
     * @throws InvalidOperationException si no es posible consultar el catálogo
     *                                   o si algún producto tiene un precio nulo
     *                                   o negativo.
     */
    @Override
    @Transactional
    public OrderResponse create(OrderCreateRequest request) {
        UUID clientId = currentUserProvider.getCurrentUserId();

        log.info(
                "Iniciando creación de orden | clientId={} | itemCount={}",
                clientId,
                request.items().size()
        );

        OrderEntity order = OrderEntity.builder()
                .clientId(clientId)
                .orderState(OrderStateEnum.CREADO)
                .total(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            log.debug(
                    "Consultando producto en catálogo | productId={}",
                    itemRequest.productId()
            );

            final BigDecimal unitPrice;

            try {
                var product = catalogClient.findById(itemRequest.productId());

                unitPrice = product.price();

            } catch (RestClientException exception) {
                log.error(
                        "No fue posible consultar el catálogo | productId={}",
                        itemRequest.productId(),
                        exception
                );

                throw new InvalidOperationException(
                        "No fue posible validar uno de los productos. Intenta nuevamente."
                );
            }

            if (unitPrice == null || unitPrice.signum() < 0) {
                log.error(
                        "Precio inválido recibido desde el catálogo | productId={}",
                        itemRequest.productId()
                );

                throw new InvalidOperationException(
                        "El producto no tiene un precio válido."
                );
            }

            BigDecimal subtotal = unitPrice.multiply(
                    BigDecimal.valueOf(itemRequest.quantity())
            );

            OrderItemEntity item = OrderItemEntity.builder()
                    .productId(itemRequest.productId())
                    .quantity(itemRequest.quantity())
                    .unitPrice(unitPrice)
                    .build();

            order.addItem(item);
            total = total.add(subtotal);
        }

        order.setTotal(total);

        OrderEntity savedOrder = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(),
                savedOrder.getId(),
                savedOrder.getItems().stream()
                        .map(item -> new OrderCreatedEvent.OrderItemEvent(
                                item.getProductId(),
                                item.getQuantity()
                        ))
                        .toList()
        );

        orderPublisherService.publishOrderCreated(event);

        log.info(
                "Orden creada | orderId={} | clientId={} | itemCount={} | total={} | status={}",
                savedOrder.getId(),
                clientId,
                savedOrder.getItems().size(),
                savedOrder.getTotal(),
                savedOrder.getOrderState()
        );

        return orderMapper.toResponse(savedOrder);
    }

    /**
     * Consulta las órdenes con filtros opcionales por cliente y estado.
     *
     * <p>Esta operación requiere un rol administrativo o de operador.
     * Si se especifican ambos filtros, se aplican conjuntamente; si solo
     * se especifica uno, se utiliza ese filtro. Si ninguno está presente,
     * se consultan todas las órdenes.</p>
     *
     * @param customerId identificador del cliente por el que se desea filtrar,
     *                   o {@code null} si no se aplica ese filtro.
     * @param status estado por el que se desea filtrar,
     *               o {@code null} si no se aplica ese filtro.
     * @param pageable configuración de paginación y ordenamiento.
     * @return página de órdenes convertidas a DTO de respuesta.
     * @throws AccessDeniedException si el usuario no tiene un rol autorizado.
     */
    @Override
    public Page<OrderResponse> findAll(
            UUID customerId,
            OrderStateEnum status,
            Pageable pageable
    ) {
        requireStaffRole();

        log.debug(
                "Consultando órdenes | customerId={} | status={} | page={} | size={}",
                customerId,
                status,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        Page<OrderEntity> orders;

        if (customerId != null && status != null) {
            orders = orderRepository.findAllByClientIdAndOrderState(
                    customerId,
                    status,
                    pageable
            );
        } else if (customerId != null) {
            orders = orderRepository.findAllByClientId(
                    customerId,
                    pageable
            );
        } else if (status != null) {
            orders = orderRepository.findAllByOrderState(
                    status,
                    pageable
            );
        } else {
            orders = orderRepository.findAll(pageable);
        }

        Page<OrderResponse> response = orders.map(orderMapper::toResponse);

        log.info(
                "Consulta administrativa completada | results={} | totalElements={}",
                response.getNumberOfElements(),
                response.getTotalElements()
        );

        return response;
    }

    /**
     * Busca una orden por su identificador y verifica el acceso según el rol.
     *
     * <p>Los usuarios con rol administrativo u operador pueden consultar
     * cualquier orden existente. Los demás usuarios solo pueden consultar
     * órdenes asociadas a su propio identificador.</p>
     *
     * @param orderId identificador único de la orden.
     * @return representación de la orden encontrada.
     * @throws ResourceNotFoundException si no existe la orden o no pertenece
     *                                   al usuario que realiza la consulta.
     */
    @Override
    public OrderResponse findById(UUID orderId) {
        log.debug("Consultando orden | orderId={}", orderId);

        OrderEntity order;

        if (isStaff()) {
            order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "La orden solicitada no existe."
                    ));
        } else {
            UUID clientId = currentUserProvider.getCurrentUserId();

            order = orderRepository.findByIdAndClientId(orderId, clientId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "La orden solicitada no existe."
                    ));
        }

        return orderMapper.toResponse(order);
    }

    /**
     * Consulta las órdenes del usuario autenticado.
     *
     * @param pageable configuración de paginación y ordenamiento.
     * @return página con las órdenes del usuario autenticado.
     */
    @Override
    public Page<OrderResponse> findMyOrders(Pageable pageable) {
        UUID clientId = currentUserProvider.getCurrentUserId();

        log.debug(
                "Consultando órdenes propias | clientId={} | page={} | size={}",
                clientId,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        Page<OrderResponse> response = orderRepository
                .findAllByClientId(clientId, pageable)
                .map(orderMapper::toResponse);

        log.info(
                "Consulta de órdenes propias completada | clientId={} | results={} | totalElements={}",
                clientId,
                response.getNumberOfElements(),
                response.getTotalElements()
        );

        return response;
    }

    /**
     * Cancela una orden perteneciente al usuario autenticado.
     *
     * <p>Solo permite cancelar órdenes que se encuentren en estado
     * {@link OrderStateEnum#CREADO}. Si la orden tiene otro estado,
     * la operación se rechaza.</p>
     *
     * @param orderId identificador único de la orden que se desea cancelar.
     * @return representación de la orden después de la cancelación.
     * @throws ResourceNotFoundException si la orden no existe o no pertenece
     *                                   al usuario autenticado.
     * @throws InvalidOperationException si la orden no está en estado
     *                                   {@code CREADO}.
     */
    @Override
    @Transactional
    public OrderResponse cancel(UUID orderId) {
        UUID clientId = currentUserProvider.getCurrentUserId();

        log.info(
                "Solicitud de cancelación | orderId={} | clientId={}",
                orderId,
                clientId
        );

        OrderEntity order = orderRepository
                .findByIdAndClientId(orderId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La orden solicitada no existe."
                ));

        if (order.getOrderState() != OrderStateEnum.CREADO) {
            log.warn(
                    "Cancelación rechazada por estado | orderId={} | status={}",
                    orderId,
                    order.getOrderState()
            );

            throw new InvalidOperationException(
                    "Solo se pueden cancelar órdenes que estén en estado CREADO."
            );
        }

        order.setOrderState(OrderStateEnum.CANCELADO);

        OrderEntity savedOrder = orderRepository.save(order);

        log.info("Orden cancelada | orderId={} | clientId={}", orderId, clientId);

        return orderMapper.toResponse(savedOrder);
    }

    /**
     * Actualiza el estado de una orden existente.
     *
     * <p>La operación requiere un rol administrativo o de operador y solo
     * permite las transiciones definidas por {@link #isValidTransition(
     * OrderStateEnum, OrderStateEnum)}.</p>
     *
     * @param orderId identificador único de la orden.
     * @param newStatus nuevo estado que se desea asignar.
     * @return representación de la orden con el estado actualizado.
     * @throws AccessDeniedException si el usuario no tiene un rol autorizado.
     * @throws ResourceNotFoundException si no existe una orden con el
     *                                   identificador indicado.
     * @throws InvalidOperationException si la transición de estado no está
     *                                   permitida.
     */
    @Override
    @Transactional
    public OrderResponse updateStatus(UUID orderId, OrderStateEnum newStatus) {
        requireStaffRole();

        log.info(
                "Solicitud de cambio de estado | orderId={} | newStatus={}",
                orderId,
                newStatus
        );

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La orden solicitada no existe."
                ));

        OrderStateEnum currentStatus = order.getOrderState();

        if (!isValidTransition(currentStatus, newStatus)) {
            log.warn(
                    "Transición de estado rechazada | orderId={} | currentStatus={} | newStatus={}",
                    orderId,
                    currentStatus,
                    newStatus
            );

            throw new InvalidOperationException(
                    "No se permite cambiar el estado de "
                            + currentStatus + " a " + newStatus + "."
            );
        }

        if (currentStatus == OrderStateEnum.CREADO
                && newStatus == OrderStateEnum.ACEPTADO) {

            log.info(
                    "Solicitando reducción de stock | orderId={}",
                    orderId
            );

            try {
                List<BulkStockReductionItemRequest> stockItems = order.getItems()
                        .stream()
                        .map(item -> new BulkStockReductionItemRequest(
                                item.getProductId(),
                                item.getQuantity()
                        ))
                        .toList();

                catalogClient.reduceStock(stockItems);

                log.info(
                        "Stock reducido correctamente | orderId={} | items={}",
                        orderId,
                        stockItems.size()
                );

            } catch (RestClientException ex) {
                log.error(
                        "No se pudo reducir el stock | orderId={}",
                        orderId,
                        ex
                );

                throw new InvalidOperationException(
                        "No se puede aceptar la orden porque no fue posible reducir el stock."
                );
            }
        }

        order.setOrderState(newStatus);

        OrderEntity savedOrder = orderRepository.save(order);

        log.info(
                "Estado actualizado | orderId={} | previousStatus={} | newStatus={}",
                orderId,
                currentStatus,
                newStatus
        );

        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse accept(UUID orderId) {
        return updateStatus(orderId, OrderStateEnum.ACEPTADO);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {

        Map<String, Long> ordersByStatus = new LinkedHashMap<>();

        for (OrderStateEnum status : OrderStateEnum.values()) {
            ordersByStatus.put(status.name(), 0L);
        }

        List<OrderStatusCountProjection> statusCounts =
                orderRepository.countOrdersByStatus();

        for (OrderStatusCountProjection projection : statusCounts) {
            ordersByStatus.put(
                    projection.getOrderState().name(),
                    projection.getTotal()
            );
        }

        long totalOrders = ordersByStatus.values()
                .stream()
                .mapToLong(Long::longValue)
                .sum();

        long pendingAcceptance =
                ordersByStatus.getOrDefault("CREADO", 0L);

        return new DashboardSummaryResponse(
                totalOrders,
                ordersByStatus,
                pendingAcceptance
        );
    }

    /**
     * Verifica si se permite pasar del estado actual al nuevo estado.
     *
     * <p>Las transiciones permitidas son:</p>
     * <ul>
     *     <li>{@code CREADO} a {@code ACEPTADO}.</li>
     *     <li>{@code ACEPTADO} a {@code EN_PREPARACION}.</li>
     *     <li>{@code EN_PREPARACION} a {@code DESPACHADO}.</li>
     *     <li>{@code DESPACHADO} a {@code ENTREGADO}.</li>
     * </ul>
     *
     * <p>No se permiten transiciones cuando alguno de los estados es nulo,
     * cuando ambos estados son iguales ni cuando el estado actual es
     * {@code ENTREGADO} o {@code CANCELADO}.</p>
     *
     * @param currentStatus estado actual de la orden.
     * @param newStatus estado al que se desea cambiar.
     * @return {@code true} si la transición está permitida; de lo contrario,
     *         {@code false}.
     */
    private boolean isValidTransition(
            OrderStateEnum currentStatus,
            OrderStateEnum newStatus
    ) {
        if (currentStatus == null || newStatus == null || currentStatus == newStatus) {
            return false;
        }

        return switch (currentStatus) {
            case CREADO ->
                    newStatus == OrderStateEnum.ACEPTADO;

            case ACEPTADO ->
                    newStatus == OrderStateEnum.EN_PREPARACION;

            case EN_PREPARACION ->
                    newStatus == OrderStateEnum.DESPACHADO;

            case DESPACHADO ->
                    newStatus == OrderStateEnum.ENTREGADO;

            case ENTREGADO, CANCELADO -> false;
        };
    }

    /**
     * Verifica que el usuario autenticado tenga permisos de administración
     * u operación.
     *
     * @throws AccessDeniedException si el usuario no posee un rol autorizado.
     */
    private void requireStaffRole() {
        if (!isStaff()) {
            throw new AccessDeniedException(
                    "No tienes permisos para realizar esta operación."
            );
        }
    }

    /**
     * Comprueba si la autenticación actual corresponde a un usuario con
     * rol administrativo u operativo.
     *
     * <p>Reconoce las autoridades {@code ROLE_ADMINISTRADOR},
     * {@code ROLE_OPERADOR}, {@code ADMINISTRADOR} y {@code OPERADOR}.</p>
     *
     * @return {@code true} si existe una autenticación válida con alguna
     *         de las autoridades reconocidas; de lo contrario, {@code false}.
     */
    private boolean isStaff() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).filter(Objects::nonNull)
                .anyMatch(role ->
                        role.equals("ROLE_ADMINISTRADOR")
                                || role.equals("ROLE_OPERADOR")
                                || role.equals("ADMINISTRADOR")
                                || role.equals("OPERADOR")
                );
    }
}
