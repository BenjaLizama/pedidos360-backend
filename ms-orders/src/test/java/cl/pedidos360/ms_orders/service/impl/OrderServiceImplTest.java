package cl.pedidos360.ms_orders.service.impl;

import cl.pedidos360.ms_orders.client.CatalogClient;
import cl.pedidos360.ms_orders.dto.request.OrderCreateRequest;
import cl.pedidos360.ms_orders.dto.request.OrderItemRequest;
import cl.pedidos360.ms_orders.dto.response.DashboardSummaryResponse;
import cl.pedidos360.ms_orders.dto.response.OrderResponse;
import cl.pedidos360.ms_orders.entity.OrderEntity;
import cl.pedidos360.ms_orders.enums.OrderStateEnum;
import cl.pedidos360.ms_orders.exception.InvalidOperationException;
import cl.pedidos360.ms_orders.exception.ResourceNotFoundException;
import cl.pedidos360.ms_orders.mapper.OrderMapper;
import cl.pedidos360.ms_orders.repository.OrderRepository;
import cl.pedidos360.ms_orders.repository.OrderStatusCountProjection;
import cl.pedidos360.ms_orders.security.CurrentUserProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    private OrderRepository orderRepository;
    private OrderMapper orderMapper;
    private CurrentUserProvider currentUserProvider;
    private CatalogClient catalogClient;
    private OrderServiceImpl orderService;

    private UUID clientId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderMapper = mock(OrderMapper.class);
        currentUserProvider = mock(CurrentUserProvider.class);
        catalogClient = mock(CatalogClient.class);

        orderService = new OrderServiceImpl(
                orderRepository,
                orderMapper,
                currentUserProvider,
                catalogClient
        );

        clientId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        when(currentUserProvider.getCurrentUserId()).thenReturn(clientId);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String role) {
        var authentication = new UsernamePasswordAuthenticationToken(
                "test-user",
                "password",
                List.of(new SimpleGrantedAuthority(role))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private OrderEntity orderWithStatus(OrderStateEnum status) {
        return OrderEntity.builder()
                .clientId(clientId)
                .orderState(status)
                .total(BigDecimal.ZERO)
                .build();
    }

    private Page<OrderEntity> emptyOrderPage() {
        return new PageImpl<>(List.of());
    }

    // ---------------------------------------------------------------------
    // create
    // ---------------------------------------------------------------------

    @Test
    void create_whenCatalogReturnsValidPrice_savesOrderAndReturnsResponse() {
        UUID productId = UUID.randomUUID();
        OrderItemRequest itemRequest = new OrderItemRequest(productId, 2);
        OrderCreateRequest request = new OrderCreateRequest(List.of(itemRequest));

        // Ajusta el tipo del DTO de producto si CatalogClient utiliza otro nombre.
        var product = mock(
                cl.pedidos360.ms_orders.dto.response.ProductResponse.class
        );
        when(product.price()).thenReturn(new BigDecimal("1500.00"));
        when(catalogClient.findById(productId)).thenReturn(product);

        when(orderRepository.save(any(OrderEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse expected = mock(OrderResponse.class);
        when(orderMapper.toResponse(any(OrderEntity.class))).thenReturn(expected);

        OrderResponse result = orderService.create(request);

        assertSame(expected, result);

        var captor = org.mockito.ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(captor.capture());

        OrderEntity savedOrder = captor.getValue();
        assertEquals(clientId, savedOrder.getClientId());
        assertEquals(OrderStateEnum.CREADO, savedOrder.getOrderState());
        assertEquals(0, new BigDecimal("3000.00").compareTo(savedOrder.getTotal()));
        assertEquals(1, savedOrder.getItems().size());

        verify(catalogClient).findById(productId);
        verify(orderMapper).toResponse(savedOrder);
    }

    @Test
    void create_whenCatalogThrowsRestClientException_throwsInvalidOperation() {
        UUID productId = UUID.randomUUID();
        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(productId, 1))
        );

        when(catalogClient.findById(productId))
                .thenThrow(new RestClientException("Catalog unavailable"));

        assertThrows(
                InvalidOperationException.class,
                () -> orderService.create(request)
        );

        verify(orderRepository, never()).save(any());
        verifyNoInteractions(orderMapper);
    }

    @Test
    void create_whenCatalogReturnsNullPrice_throwsInvalidOperation() {
        UUID productId = UUID.randomUUID();
        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(productId, 1))
        );

        var product = mock(
                cl.pedidos360.ms_orders.dto.response.ProductResponse.class
        );
        when(product.price()).thenReturn(null);
        when(catalogClient.findById(productId)).thenReturn(product);

        assertThrows(
                InvalidOperationException.class,
                () -> orderService.create(request)
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void create_whenCatalogReturnsNegativePrice_throwsInvalidOperation() {
        UUID productId = UUID.randomUUID();
        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(productId, 1))
        );

        var product = mock(
                cl.pedidos360.ms_orders.dto.response.ProductResponse.class
        );
        when(product.price()).thenReturn(new BigDecimal("-1.00"));
        when(catalogClient.findById(productId)).thenReturn(product);

        assertThrows(
                InvalidOperationException.class,
                () -> orderService.create(request)
        );

        verify(orderRepository, never()).save(any());
    }

    // ---------------------------------------------------------------------
    // findAll
    // ---------------------------------------------------------------------

    @Test
    void findAll_whenUserIsNotStaff_throwsAccessDenied() {
        SecurityContextHolder.clearContext();

        assertThrows(
                AccessDeniedException.class,
                () -> orderService.findAll(null, null, PageRequest.of(0, 10))
        );

        verifyNoInteractions(orderRepository);
    }

    @Test
    void findAll_withoutFilters_queriesAllOrders() {
        authenticateAs("ADMINISTRADOR");

        PageRequest pageable = PageRequest.of(0, 10);
        when(orderRepository.findAll(pageable)).thenReturn(emptyOrderPage());

        Page<OrderResponse> result =
                orderService.findAll(null, null, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(orderRepository).findAll(pageable);
    }

    @Test
    void findAll_withCustomerFilter_queriesOrdersByClient() {
        authenticateAs("ROLE_ADMINISTRADOR");

        PageRequest pageable = PageRequest.of(0, 10);
        when(orderRepository.findAllByClientId(clientId, pageable))
                .thenReturn(emptyOrderPage());

        Page<OrderResponse> result =
                orderService.findAll(clientId, null, pageable);

        assertNotNull(result);
        verify(orderRepository).findAllByClientId(clientId, pageable);
        verify(orderRepository, never()).findAllByOrderState(
                any(OrderStateEnum.class), any()
        );
    }

    @Test
    void findAll_withStatusFilter_queriesOrdersByStatus() {
        authenticateAs("ROLE_OPERADOR");

        PageRequest pageable = PageRequest.of(0, 10);
        OrderStateEnum status = OrderStateEnum.CREADO;

        when(orderRepository.findAllByOrderState(status, pageable))
                .thenReturn(emptyOrderPage());

        Page<OrderResponse> result =
                orderService.findAll(null, status, pageable);

        assertNotNull(result);
        verify(orderRepository).findAllByOrderState(status, pageable);
    }

    @Test
    void findAll_withCustomerAndStatusFilters_queriesOrdersByBoth() {
        authenticateAs("ROLE_ADMINISTRADOR");

        PageRequest pageable = PageRequest.of(0, 10);
        OrderStateEnum status = OrderStateEnum.CREADO;

        when(orderRepository.findAllByClientIdAndOrderState(
                clientId, status, pageable
        )).thenReturn(emptyOrderPage());

        Page<OrderResponse> result =
                orderService.findAll(clientId, status, pageable);

        assertNotNull(result);
        verify(orderRepository).findAllByClientIdAndOrderState(
                clientId, status, pageable
        );
    }

    // ---------------------------------------------------------------------
    // findById
    // ---------------------------------------------------------------------

    @Test
    void findById_whenOrderDoesNotExistForCustomer_throwsNotFound() {
        SecurityContextHolder.clearContext();

        when(orderRepository.findByIdAndClientId(orderId, clientId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.findById(orderId)
        );

        verify(orderRepository).findByIdAndClientId(orderId, clientId);
        verifyNoInteractions(orderMapper);
    }

    @Test
    void findById_whenCustomerOwnsOrder_returnsMappedOrder() {
        SecurityContextHolder.clearContext();

        OrderEntity order = orderWithStatus(OrderStateEnum.CREADO);
        OrderResponse expected = mock(OrderResponse.class);

        when(orderRepository.findByIdAndClientId(orderId, clientId))
                .thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(expected);

        OrderResponse result = orderService.findById(orderId);

        assertSame(expected, result);
        verify(orderRepository).findByIdAndClientId(orderId, clientId);
        verify(orderMapper).toResponse(order);
    }

    @Test
    void findById_whenStaffAndOrderExists_returnsMappedOrder() {
        authenticateAs("ROLE_ADMINISTRADOR");

        OrderEntity order = orderWithStatus(OrderStateEnum.CREADO);
        OrderResponse expected = mock(OrderResponse.class);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(expected);

        OrderResponse result = orderService.findById(orderId);

        assertSame(expected, result);
        verify(orderRepository).findById(orderId);
        verify(orderMapper).toResponse(order);
        verifyNoInteractions(currentUserProvider);
    }

    @Test
    void findById_whenStaffAndOrderDoesNotExist_throwsNotFound() {
        authenticateAs("ROLE_OPERADOR");

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.findById(orderId)
        );

        verify(orderRepository).findById(orderId);
        verifyNoInteractions(orderMapper);
    }

    // ---------------------------------------------------------------------
    // findMyOrders
    // ---------------------------------------------------------------------

    @Test
    void findMyOrders_returnsOrdersForAuthenticatedClient() {
        PageRequest pageable = PageRequest.of(0, 10);

        when(orderRepository.findAllByClientId(clientId, pageable))
                .thenReturn(emptyOrderPage());

        Page<OrderResponse> result = orderService.findMyOrders(pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(currentUserProvider).getCurrentUserId();
        verify(orderRepository).findAllByClientId(clientId, pageable);
    }

    // ---------------------------------------------------------------------
    // cancel
    // ---------------------------------------------------------------------

    @Test
    void cancel_whenOrderDoesNotExist_throwsNotFound() {
        when(orderRepository.findByIdAndClientId(orderId, clientId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.cancel(orderId)
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancel_whenOrderIsNotCreated_throwsInvalidOperation() {
        OrderEntity order = orderWithStatus(OrderStateEnum.ACEPTADO);

        when(orderRepository.findByIdAndClientId(orderId, clientId))
                .thenReturn(Optional.of(order));

        assertThrows(
                InvalidOperationException.class,
                () -> orderService.cancel(orderId)
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancel_whenOrderIsCreated_cancelsAndReturnsMappedOrder() {
        OrderEntity order = orderWithStatus(OrderStateEnum.CREADO);
        OrderResponse expected = mock(OrderResponse.class);

        when(orderRepository.findByIdAndClientId(orderId, clientId))
                .thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(expected);

        OrderResponse result = orderService.cancel(orderId);

        assertSame(expected, result);
        assertEquals(OrderStateEnum.CANCELADO, order.getOrderState());
        verify(orderRepository).save(order);
        verify(orderMapper).toResponse(order);
    }

    // ---------------------------------------------------------------------
    // updateStatus / accept
    // ---------------------------------------------------------------------

    @Test
    void updateStatus_whenUserIsNotStaff_throwsAccessDenied() {
        SecurityContextHolder.clearContext();

        assertThrows(
                AccessDeniedException.class,
                () -> orderService.updateStatus(orderId, OrderStateEnum.ACEPTADO)
        );

        verifyNoInteractions(orderRepository);
    }

    @Test
    void updateStatus_whenOrderDoesNotExist_throwsNotFound() {
        authenticateAs("ROLE_ADMINISTRADOR");
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.updateStatus(orderId, OrderStateEnum.ACEPTADO)
        );

        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateStatus_whenTransitionIsInvalid_throwsInvalidOperation() {
        authenticateAs("ROLE_ADMINISTRADOR");

        OrderEntity order = orderWithStatus(OrderStateEnum.CREADO);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(
                InvalidOperationException.class,
                () -> orderService.updateStatus(orderId, OrderStateEnum.ENTREGADO)
        );

        verify(orderRepository, never()).save(any());
        verifyNoInteractions(catalogClient);
    }

    @Test
    void updateStatus_whenNewStatusEqualsCurrentStatus_throwsInvalidOperation() {
        authenticateAs("ROLE_ADMINISTRADOR");

        OrderEntity order = orderWithStatus(OrderStateEnum.ACEPTADO);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(
                InvalidOperationException.class,
                () -> orderService.updateStatus(orderId, OrderStateEnum.ACEPTADO)
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateStatus_whenMovingFromCreatedToAccepted_reducesStockAndSaves() {
        authenticateAs("ROLE_ADMINISTRADOR");

        OrderEntity order = orderWithStatus(OrderStateEnum.CREADO);
        OrderResponse expected = mock(OrderResponse.class);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(expected);

        OrderResponse result =
                orderService.updateStatus(orderId, OrderStateEnum.ACEPTADO);

        assertSame(expected, result);
        assertEquals(OrderStateEnum.ACEPTADO, order.getOrderState());
        verify(catalogClient).reduceStock(anyList());
        verify(orderRepository).save(order);
        verify(orderMapper).toResponse(order);
    }

    @Test
    void updateStatus_whenAcceptingAndCatalogFails_throwsInvalidOperation() {
        authenticateAs("ROLE_ADMINISTRADOR");

        OrderEntity order = orderWithStatus(OrderStateEnum.CREADO);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        doThrow(new RestClientException("Catalog unavailable"))
                .when(catalogClient).reduceStock(anyList());

        assertThrows(
                InvalidOperationException.class,
                () -> orderService.updateStatus(orderId, OrderStateEnum.ACEPTADO)
        );

        verify(catalogClient).reduceStock(anyList());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateStatus_whenMovingFromAcceptedToPreparation_savesNewStatus() {
        authenticateAs("ROLE_OPERADOR");

        OrderEntity order = orderWithStatus(OrderStateEnum.ACEPTADO);
        OrderResponse expected = mock(OrderResponse.class);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(expected);

        OrderResponse result = orderService.updateStatus(
                orderId, OrderStateEnum.EN_PREPARACION
        );

        assertSame(expected, result);
        assertEquals(OrderStateEnum.EN_PREPARACION, order.getOrderState());
        verify(orderRepository).save(order);
    }

    @Test
    void updateStatus_whenMovingFromPreparationToDispatched_savesNewStatus() {
        authenticateAs("ROLE_OPERADOR");

        OrderEntity order = orderWithStatus(OrderStateEnum.EN_PREPARACION);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.updateStatus(orderId, OrderStateEnum.DESPACHADO);

        assertEquals(OrderStateEnum.DESPACHADO, order.getOrderState());
        verify(orderRepository).save(order);
    }

    @Test
    void updateStatus_whenMovingFromDispatchedToDelivered_savesNewStatus() {
        authenticateAs("ROLE_OPERADOR");

        OrderEntity order = orderWithStatus(OrderStateEnum.DESPACHADO);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.updateStatus(orderId, OrderStateEnum.ENTREGADO);

        assertEquals(OrderStateEnum.ENTREGADO, order.getOrderState());
        verify(orderRepository).save(order);
    }

    @Test
    void updateStatus_whenOrderIsDelivered_rejectsFurtherTransition() {
        authenticateAs("ROLE_ADMINISTRADOR");

        OrderEntity order = orderWithStatus(OrderStateEnum.ENTREGADO);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(
                InvalidOperationException.class,
                () -> orderService.updateStatus(orderId, OrderStateEnum.CREADO)
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateStatus_whenOrderIsCancelled_rejectsFurtherTransition() {
        authenticateAs("ROLE_ADMINISTRADOR");

        OrderEntity order = orderWithStatus(OrderStateEnum.CANCELADO);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(
                InvalidOperationException.class,
                () -> orderService.updateStatus(orderId, OrderStateEnum.CREADO)
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void accept_delegatesToUpdateStatus() {
        authenticateAs("ROLE_ADMINISTRADOR");

        OrderEntity order = orderWithStatus(OrderStateEnum.CREADO);
        ReflectionTestUtils.setField(order, "id", orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        orderService.accept(orderId);

        assertEquals(OrderStateEnum.ACEPTADO, order.getOrderState());
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(order);
        verify(catalogClient).reduceStock(anyList());
    }

    // ---------------------------------------------------------------------
    // Dashboard
    // ---------------------------------------------------------------------

    @Test
    void getDashboardSummary_whenRepositoryReturnsCounts_calculatesTotals() {
        OrderStatusCountProjection created = mock(OrderStatusCountProjection.class);
        when(created.getOrderState()).thenReturn(OrderStateEnum.CREADO);
        when(created.getTotal()).thenReturn(4L);

        OrderStatusCountProjection accepted = mock(OrderStatusCountProjection.class);
        when(accepted.getOrderState()).thenReturn(OrderStateEnum.ACEPTADO);
        when(accepted.getTotal()).thenReturn(6L);

        when(orderRepository.countOrdersByStatus())
                .thenReturn(List.of(created, accepted));

        DashboardSummaryResponse result = orderService.getDashboardSummary();

        assertEquals(10L, result.totalOrders());
        assertEquals(4L, result.pendingAcceptance());
        assertEquals(4L, result.orderByStatus().get("CREADO"));
        assertEquals(6L, result.orderByStatus().get("ACEPTADO"));

        // Los estados sin órdenes deben aparecer con cero.
        for (OrderStateEnum status : OrderStateEnum.values()) {
            assertTrue(result.orderByStatus().containsKey(status.name()));
        }

        verify(orderRepository).countOrdersByStatus();
    }

    @Test
    void getDashboardSummary_whenThereAreNoOrders_returnsZeroTotals() {
        when(orderRepository.countOrdersByStatus()).thenReturn(List.of());

        DashboardSummaryResponse result = orderService.getDashboardSummary();

        assertEquals(0L, result.totalOrders());
        assertEquals(0L, result.pendingAcceptance());

        for (OrderStateEnum status : OrderStateEnum.values()) {
            assertEquals(0L, result.orderByStatus().get(status.name()));
        }
    }
}
