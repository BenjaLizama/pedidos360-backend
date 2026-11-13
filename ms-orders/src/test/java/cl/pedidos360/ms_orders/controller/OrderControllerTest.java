package cl.pedidos360.ms_orders.controller;

import cl.pedidos360.ms_orders.dto.request.OrderCreateRequest;
import cl.pedidos360.ms_orders.dto.request.OrderItemRequest;
import cl.pedidos360.ms_orders.dto.request.OrderStatusUpdateRequest;
import cl.pedidos360.ms_orders.dto.response.DashboardSummaryResponse;
import cl.pedidos360.ms_orders.dto.response.OrderResponse;
import cl.pedidos360.ms_orders.enums.OrderStateEnum;
import cl.pedidos360.ms_orders.mapper.ErrorResponseMapper;
import cl.pedidos360.ms_orders.security.JwtProperties;
import cl.pedidos360.ms_orders.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@EnableConfigurationProperties(JwtProperties.class)
class OrderControllerTest {

    @TestConfiguration(proxyBeanMethods = false)
    static class TestConfig {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    private static final String BASE_URL = "/api/v1/orders";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private ErrorResponseMapper errorResponseMapper;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final UUID orderId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();

    private OrderResponse orderResponse() {
        return new OrderResponse(
                orderId,
                customerId,
                OrderStateEnum.CREADO,
                new BigDecimal("15000.00"),
                List.of(),
                Instant.parse("2026-01-01T10:00:00Z"),
                Instant.parse("2026-01-01T10:00:00Z")
        );
    }

    private Page<OrderResponse> orderPage() {
        return new PageImpl<>(
                List.of(orderResponse()),
                PageRequest.of(0, 10),
                1
        );
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void createShouldReturn201() throws Exception {
        OrderItemRequest item = new OrderItemRequest(
                UUID.randomUUID(),
                1
        );

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(item)
        );

        when(orderService.create(any(OrderCreateRequest.class)))
                .thenReturn(orderResponse());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message")
                        .value("Orden creada con éxito."))
                .andExpect(jsonPath("$.data.id")
                        .value(orderId.toString()));

        verify(orderService).create(any(OrderCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    void findAllShouldReturn200() throws Exception {
        when(orderService.findAll(
                any(UUID.class),
                any(OrderStateEnum.class),
                any(Pageable.class)
        )).thenReturn(orderPage());

        mockMvc.perform(get(BASE_URL)
                        .param("customerId", customerId.toString())
                        .param("status", "CREADO")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Órdenes consultadas con éxito."));

        verify(orderService).findAll(
                eq(customerId),
                eq(OrderStateEnum.CREADO),
                any(Pageable.class)
        );
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void findMyOrdersShouldReturn200() throws Exception {
        when(orderService.findMyOrders(any(Pageable.class)))
                .thenReturn(orderPage());

        mockMvc.perform(get(BASE_URL + "/me")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Tus órdenes fueron consultadas con éxito."));

        verify(orderService).findMyOrders(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void findByIdShouldReturn200() throws Exception {
        when(orderService.findById(orderId))
                .thenReturn(orderResponse());

        mockMvc.perform(get(BASE_URL + "/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Orden consultada con éxito."))
                .andExpect(jsonPath("$.data.id")
                        .value(orderId.toString()));

        verify(orderService).findById(orderId);
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void cancelShouldReturn200() throws Exception {
        when(orderService.cancel(orderId))
                .thenReturn(orderResponse());

        mockMvc.perform(patch(BASE_URL + "/{orderId}/cancel", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Orden cancelada con éxito."));

        verify(orderService).cancel(orderId);
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    void cancelShouldReturn200ForStaff() throws Exception {
        when(orderService.cancel(orderId))
                .thenReturn(orderResponse());

        mockMvc.perform(patch(BASE_URL + "/{orderId}/cancel", orderId))
                .andExpect(status().isOk());

        verify(orderService).cancel(orderId);
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    void updateStatusShouldReturn200() throws Exception {
        OrderStatusUpdateRequest request =
                new OrderStatusUpdateRequest(OrderStateEnum.CREADO);

        when(orderService.updateStatus(orderId, OrderStateEnum.CREADO))
                .thenReturn(orderResponse());

        mockMvc.perform(put(BASE_URL + "/{orderId}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Estado de la orden actualizado con éxito."));

        verify(orderService).updateStatus(
                orderId,
                OrderStateEnum.CREADO
        );
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    void acceptShouldReturn200() throws Exception {
        when(orderService.accept(orderId))
                .thenReturn(orderResponse());

        mockMvc.perform(patch(BASE_URL + "/{orderId}/accept", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Orden aceptada y stock actualizado con éxito."));

        verify(orderService).accept(orderId);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void getDashboardSummaryShouldReturn200() throws Exception {
        DashboardSummaryResponse summary =
                new DashboardSummaryResponse(
                        10L,
                        Map.of("CREADO", 4L, "ACEPTADO", 6L),
                        4L
                );

        when(orderService.getDashboardSummary())
                .thenReturn(summary);

        mockMvc.perform(get(BASE_URL + "/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Resumen del dashboard obtenido correctamente"))
                .andExpect(jsonPath("$.data.totalOrders").value(10))
                .andExpect(jsonPath("$.data.pendingAcceptance").value(4));

        verify(orderService).getDashboardSummary();
    }
}
