package cl.pedidos360.ms_orders.service.impl;

import cl.pedidos360.ms_orders.client.CatalogClient;
import cl.pedidos360.ms_orders.dto.response.OrderResponse;
import cl.pedidos360.ms_orders.entity.OrderEntity;
import cl.pedidos360.ms_orders.enums.OrderStateEnum;
import cl.pedidos360.ms_orders.exception.InvalidOperationException;
import cl.pedidos360.ms_orders.exception.ResourceNotFoundException;
import cl.pedidos360.ms_orders.mapper.OrderMapper;
import cl.pedidos360.ms_orders.repository.OrderRepository;
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

    @Test
    void findAll_whenUserIsNotStaff_throwsAccessDenied() {
        SecurityContextHolder.clearContext();

        assertThrows(
                AccessDeniedException.class,
                () -> orderService.findAll(
                        null,
                        null,
                        PageRequest.of(0, 10)
                )
        );

        verifyNoInteractions(orderRepository);
    }

    @Test
    void findAll_withoutFilters_queriesAllOrders() {
        authenticateAs("ADMINISTRADOR");

        PageRequest pageable = PageRequest.of(0, 10);
        Page<OrderEntity> entities = new PageImpl<>(List.of());

        when(orderRepository.findAll(pageable)).thenReturn(entities);

        Page<OrderResponse> result =
                orderService.findAll(null, null, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(orderRepository).findAll(pageable);
    }

    @Test
    void findMyOrders_returnsOrdersForAuthenticatedClient() {
        PageRequest pageable = PageRequest.of(0, 10);

        when(orderRepository.findAllByClientId(clientId, pageable))
                .thenReturn(new PageImpl<>(List.of()));

        Page<OrderResponse> result = orderService.findMyOrders(pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        verify(currentUserProvider).getCurrentUserId();
        verify(orderRepository).findAllByClientId(clientId, pageable);
    }

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

    @Test
    void updateStatus_whenUserIsNotStaff_throwsAccessDenied() {
        SecurityContextHolder.clearContext();

        assertThrows(
                AccessDeniedException.class,
                () -> orderService.updateStatus(
                        orderId,
                        OrderStateEnum.ACEPTADO
                )
        );

        verifyNoInteractions(orderRepository);
    }

    @Test
    void updateStatus_whenOrderDoesNotExist_throwsNotFound() {
        authenticateAs("ROLE_ADMINISTRADOR");

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.updateStatus(
                        orderId,
                        OrderStateEnum.ACEPTADO
                )
        );
    }

    @Test
    void updateStatus_whenTransitionIsInvalid_throwsInvalidOperation() {
        authenticateAs("ROLE_ADMINISTRADOR");

        OrderEntity order = orderWithStatus(OrderStateEnum.CREADO);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(
                InvalidOperationException.class,
                () -> orderService.updateStatus(
                        orderId,
                        OrderStateEnum.ENTREGADO
                )
        );

        verify(orderRepository, never()).save(any());
        verifyNoInteractions(catalogClient);
    }

    @Test
    void updateStatus_whenAcceptingAndCatalogFails_throwsInvalidOperation() {
        authenticateAs("ROLE_ADMINISTRADOR");

        OrderEntity order = orderWithStatus(OrderStateEnum.CREADO);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        doThrow(new RestClientException("Catalog unavailable"))
                .when(catalogClient).reduceStock(any());

        assertThrows(
                InvalidOperationException.class,
                () -> orderService.updateStatus(
                        orderId,
                        OrderStateEnum.ACEPTADO
                )
        );

        verify(catalogClient).reduceStock(any());
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
                orderId,
                OrderStateEnum.EN_PREPARACION
        );

        assertSame(expected, result);
        assertEquals(OrderStateEnum.EN_PREPARACION, order.getOrderState());
        verify(orderRepository).save(order);
    }

    @Test
    void accept_delegatesToUpdateStatus() {
        UUID orderId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "test-admin",
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")
                        )
                )
        );

        OrderEntity order = orderWithStatus(OrderStateEnum.CREADO);
        ReflectionTestUtils.setField(order, "id", orderId);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(OrderEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        orderService.accept(orderId);

        assertEquals(OrderStateEnum.ACEPTADO, order.getOrderState());

        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(order);
        verify(catalogClient).reduceStock(any());
    }
}
