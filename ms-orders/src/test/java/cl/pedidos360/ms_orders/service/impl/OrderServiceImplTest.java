package cl.pedidos360.ms_orders.service.impl;

import cl.pedidos360.ms_orders.client.CatalogClient;
import cl.pedidos360.ms_orders.dto.request.OrderCreateRequest;
import cl.pedidos360.ms_orders.dto.request.OrderItemRequest;
import cl.pedidos360.ms_orders.dto.response.OrderResponse;
import cl.pedidos360.ms_orders.dto.response.ProductResponse;
import cl.pedidos360.ms_orders.entity.OrderEntity;
import cl.pedidos360.ms_orders.enums.OrderStateEnum;
import cl.pedidos360.ms_orders.enums.ProductStatus;
import cl.pedidos360.ms_orders.exception.InvalidOperationException;
import cl.pedidos360.ms_orders.exception.ResourceNotFoundException;
import cl.pedidos360.ms_orders.mapper.OrderMapper;
import cl.pedidos360.ms_orders.repository.OrderRepository;
import cl.pedidos360.ms_orders.security.CurrentUserProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private static final UUID CLIENT_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID OTHER_CLIENT_ID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final UUID ORDER_ID =
            UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final UUID PRODUCT_ID =
            UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private CatalogClient catalogClient;

    @Mock
    private OrderResponse orderResponse;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        CLIENT_ID.toString(),
                        "test",
                        List.of()
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Test
    void shouldCreateOrderAndCalculateTotal() {
        mockCurrentUser();
        mockSave();
        mockOrderMapper();

        when(catalogClient.findById(PRODUCT_ID))
                .thenReturn(product(PRODUCT_ID, "2500.00"));

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(PRODUCT_ID, 2))
        );

        OrderResponse result = orderService.create(request);

        assertThat(result).isSameAs(orderResponse);

        verify(catalogClient).findById(PRODUCT_ID);

        verify(orderRepository).save(argThat(order ->
                order.getClientId().equals(CLIENT_ID)
                        && order.getOrderState() == OrderStateEnum.CREADO
                        && order.getTotal().compareTo(
                        new BigDecimal("5000.00")
                ) == 0
                        && order.getItems().size() == 1
        ));

        verify(orderMapper).toResponse(any(OrderEntity.class));
    }

    @Test
    void shouldThrowInvalidOperationWhenCatalogClientFails() {
        mockCurrentUser();

        when(catalogClient.findById(PRODUCT_ID))
                .thenThrow(new RestClientException("Error de comunicación"));

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(PRODUCT_ID, 1))
        );

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(InvalidOperationException.class);

        verify(orderRepository, never()).save(any(OrderEntity.class));
        verifyNoInteractions(orderMapper);
    }

    @Test
    void shouldThrowInvalidOperationWhenProductPriceIsNull() {
        mockCurrentUser();

        when(catalogClient.findById(PRODUCT_ID))
                .thenReturn(product(PRODUCT_ID, null));

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(PRODUCT_ID, 1))
        );

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(InvalidOperationException.class);

        verify(orderRepository, never()).save(any(OrderEntity.class));
        verifyNoInteractions(orderMapper);
    }

    @Test
    void shouldThrowInvalidOperationWhenProductPriceIsNegative() {
        mockCurrentUser();

        when(catalogClient.findById(PRODUCT_ID))
                .thenReturn(product(PRODUCT_ID, "-10.00"));

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(PRODUCT_ID, 1))
        );

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(InvalidOperationException.class);

        verify(orderRepository, never()).save(any(OrderEntity.class));
        verifyNoInteractions(orderMapper);
    }

    // -------------------------------------------------------------------------
    // FIND ALL
    // -------------------------------------------------------------------------

    @Test
    void shouldFindAllOrdersWhenUserIsStaff() {
        setStaffAuthentication();
        mockOrderMapper();

        PageRequest pageable = PageRequest.of(0, 10);
        OrderEntity order = createOrder(CLIENT_ID, OrderStateEnum.CREADO);

        when(orderRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(order), pageable, 1));

        Page<OrderResponse> result =
                orderService.findAll(null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(orderResponse);

        verify(orderRepository).findAll(pageable);
        verify(orderMapper).toResponse(order);
    }

    @Test
    void shouldFindOrdersByCustomerAndStatus() {
        setStaffAuthentication();
        mockOrderMapper();

        PageRequest pageable = PageRequest.of(0, 10);
        OrderEntity order = createOrder(CLIENT_ID, OrderStateEnum.CREADO);

        when(orderRepository.findAllByClientIdAndOrderState(
                CLIENT_ID, OrderStateEnum.CREADO, pageable
        )).thenReturn(new PageImpl<>(List.of(order), pageable, 1));

        Page<OrderResponse> result = orderService.findAll(
                CLIENT_ID,
                OrderStateEnum.CREADO,
                pageable
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(orderResponse);

        verify(orderRepository).findAllByClientIdAndOrderState(
                CLIENT_ID, OrderStateEnum.CREADO, pageable
        );
    }

    @Test
    void shouldDenyFindAllForNonStaff() {
        PageRequest pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() ->
                orderService.findAll(null, null, pageable)
        ).isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(orderRepository, orderMapper);
    }

    // -------------------------------------------------------------------------
    // FIND BY ID
    // -------------------------------------------------------------------------

    @Test
    void shouldFindOwnOrderById() {
        mockCurrentUser();
        mockOrderMapper();

        OrderEntity order = createOrder(CLIENT_ID, OrderStateEnum.CREADO);

        when(orderRepository.findByIdAndClientId(ORDER_ID, CLIENT_ID))
                .thenReturn(Optional.of(order));

        OrderResponse result = orderService.findById(ORDER_ID);

        assertThat(result).isSameAs(orderResponse);

        verify(orderRepository).findByIdAndClientId(ORDER_ID, CLIENT_ID);
        verify(orderMapper).toResponse(order);
    }

    @Test
    void shouldNotFindOrderOwnedByAnotherClient() {
        mockCurrentUser();

        when(orderRepository.findByIdAndClientId(ORDER_ID, CLIENT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(ORDER_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(orderRepository).findByIdAndClientId(ORDER_ID, CLIENT_ID);
        verifyNoInteractions(orderMapper);
    }

    @Test
    void shouldAllowStaffToFindAnyOrderById() {
        setStaffAuthentication();
        mockOrderMapper();

        OrderEntity order = createOrder(OTHER_CLIENT_ID, OrderStateEnum.CREADO);

        when(orderRepository.findById(ORDER_ID))
                .thenReturn(Optional.of(order));

        OrderResponse result = orderService.findById(ORDER_ID);

        assertThat(result).isSameAs(orderResponse);

        verify(orderRepository).findById(ORDER_ID);
        verify(orderMapper).toResponse(order);
    }

    // -------------------------------------------------------------------------
    // FIND MY ORDERS
    // -------------------------------------------------------------------------

    @Test
    void shouldFindOrdersOfAuthenticatedClient() {
        mockCurrentUser();
        mockOrderMapper();

        PageRequest pageable = PageRequest.of(0, 10);
        OrderEntity order = createOrder(CLIENT_ID, OrderStateEnum.CREADO);

        when(orderRepository.findAllByClientId(CLIENT_ID, pageable))
                .thenReturn(new PageImpl<>(List.of(order), pageable, 1));

        Page<OrderResponse> result = orderService.findMyOrders(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(orderResponse);

        verify(orderRepository).findAllByClientId(CLIENT_ID, pageable);
        verify(orderMapper).toResponse(order);
    }

    // -------------------------------------------------------------------------
    // CANCEL
    // -------------------------------------------------------------------------

    @Test
    void shouldCancelOrderWhenItIsCreatedAndBelongsToClient() {
        mockCurrentUser();
        mockSave();
        mockOrderMapper();

        OrderEntity order = createOrder(CLIENT_ID, OrderStateEnum.CREADO);

        when(orderRepository.findByIdAndClientId(ORDER_ID, CLIENT_ID))
                .thenReturn(Optional.of(order));

        OrderResponse result = orderService.cancel(ORDER_ID);

        assertThat(result).isSameAs(orderResponse);
        assertThat(order.getOrderState()).isEqualTo(OrderStateEnum.CANCELADO);

        verify(orderRepository).save(order);
        verify(orderMapper).toResponse(order);
    }

    @Test
    void shouldThrowWhenCancellingOrderNotOwnedByClient() {
        mockCurrentUser();

        when(orderRepository.findByIdAndClientId(ORDER_ID, CLIENT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancel(ORDER_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(orderRepository, never()).save(any(OrderEntity.class));
        verifyNoInteractions(orderMapper);
    }

    @Test
    void shouldRejectCancellationWhenOrderIsNotCreated() {
        mockCurrentUser();

        OrderEntity order = createOrder(CLIENT_ID, OrderStateEnum.ACEPTADO);

        when(orderRepository.findByIdAndClientId(ORDER_ID, CLIENT_ID))
                .thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancel(ORDER_ID))
                .isInstanceOf(InvalidOperationException.class);

        verify(orderRepository, never()).save(any(OrderEntity.class));
        verifyNoInteractions(orderMapper);
    }

    // -------------------------------------------------------------------------
    // UPDATE STATUS
    // -------------------------------------------------------------------------

    @Test
    void shouldUpdateOrderStatusWhenTransitionIsValid() {
        setStaffAuthentication();
        mockSave();
        mockOrderMapper();

        OrderEntity order = createOrder(CLIENT_ID, OrderStateEnum.CREADO);

        when(orderRepository.findById(ORDER_ID))
                .thenReturn(Optional.of(order));

        OrderResponse result = orderService.updateStatus(
                ORDER_ID,
                OrderStateEnum.ACEPTADO
        );

        assertThat(result).isSameAs(orderResponse);
        assertThat(order.getOrderState()).isEqualTo(OrderStateEnum.ACEPTADO);

        verify(orderRepository).save(order);
        verify(orderMapper).toResponse(order);
    }

    @Test
    void shouldDenyStatusUpdateForNonStaff() {
        assertThatThrownBy(() ->
                orderService.updateStatus(ORDER_ID, OrderStateEnum.ACEPTADO)
        ).isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(orderRepository, orderMapper);
    }

    @Test
    void shouldThrowWhenOrderToUpdateDoesNotExist() {
        setStaffAuthentication();

        when(orderRepository.findById(ORDER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                orderService.updateStatus(ORDER_ID, OrderStateEnum.ACEPTADO)
        ).isInstanceOf(ResourceNotFoundException.class);

        verify(orderRepository, never()).save(any(OrderEntity.class));
        verifyNoInteractions(orderMapper);
    }

    @Test
    void shouldRejectInvalidStatusTransition() {
        setStaffAuthentication();

        OrderEntity order = createOrder(CLIENT_ID, OrderStateEnum.CREADO);

        when(orderRepository.findById(ORDER_ID))
                .thenReturn(Optional.of(order));

        assertThatThrownBy(() ->
                orderService.updateStatus(ORDER_ID, OrderStateEnum.DESPACHADO)
        ).isInstanceOf(InvalidOperationException.class);

        verify(orderRepository, never()).save(any(OrderEntity.class));
        verifyNoInteractions(orderMapper);
    }

    @Test
    void shouldRejectStatusTransitionFromFinalState() {
        setStaffAuthentication();

        OrderEntity order = createOrder(CLIENT_ID, OrderStateEnum.ENTREGADO);

        when(orderRepository.findById(ORDER_ID))
                .thenReturn(Optional.of(order));

        assertThatThrownBy(() ->
                orderService.updateStatus(ORDER_ID, OrderStateEnum.CANCELADO)
        ).isInstanceOf(InvalidOperationException.class);

        verify(orderRepository, never()).save(any(OrderEntity.class));
        verifyNoInteractions(orderMapper);
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private void mockCurrentUser() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(CLIENT_ID);
    }

    private void mockSave() {
        when(orderRepository.save(any(OrderEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void mockOrderMapper() {
        when(orderMapper.toResponse(any(OrderEntity.class)))
                .thenReturn(orderResponse);
    }

    private void setStaffAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "staff-user",
                        "test",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))
                )
        );
    }

    private OrderEntity createOrder(UUID clientId, OrderStateEnum state) {
        return OrderEntity.builder()
                .clientId(clientId)
                .orderState(state)
                .total(new BigDecimal("10000.00"))
                .build();
    }

    private ProductResponse product(UUID productId, String price) {
        return new ProductResponse(
                productId,
                "SKU-TEST",
                "Producto de prueba",
                "Descripción de prueba",
                price == null ? null : new BigDecimal(price),
                100,
                UUID.randomUUID(),
                ProductStatus.values()[0],
                Instant.now(),
                Instant.now()
        );
    }
}
