package cl.pedidos360.ms_orders.service.impl;

import cl.pedidos360.ms_orders.client.CatalogClient;
import cl.pedidos360.ms_orders.dto.request.OrderCreateRequest;
import cl.pedidos360.ms_orders.dto.request.OrderItemRequest;
import cl.pedidos360.ms_orders.dto.response.OrderResponse;
import cl.pedidos360.ms_orders.entity.OrderEntity;
import cl.pedidos360.ms_orders.entity.OrderItemEntity;
import cl.pedidos360.ms_orders.enums.OrderStateEnum;
import cl.pedidos360.ms_orders.exception.InvalidOperationException;
import cl.pedidos360.ms_orders.exception.ResourceNotFoundException;
import cl.pedidos360.ms_orders.mapper.OrderMapper;
import cl.pedidos360.ms_orders.repository.OrderRepository;
import cl.pedidos360.ms_orders.security.CurrentUserProvider;
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
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CurrentUserProvider currentUserProvider;
    private final CatalogClient catalogClient;

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

                // Se asume que ProductResponse expone price() como BigDecimal.
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

    private void requireStaffRole() {
        if (!isStaff()) {
            throw new AccessDeniedException(
                    "No tienes permisos para realizar esta operación."
            );
        }
    }

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
