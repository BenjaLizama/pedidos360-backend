package cl.eventpass.bff.controller;

import cl.eventpass.bff.client.CatalogClient;
import cl.eventpass.bff.client.OrdersClient;
import cl.eventpass.bff.dto.response.OrderResponse;
import cl.eventpass.bff.dto.response.PageResponse;
import cl.eventpass.bff.dto.response.StandardResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class OrderControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private CatalogClient catalogClient;

    @MockitoBean
    private OrdersClient ordersClient;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("GET /api/v1/orders with pagination parameters resolves Pageable successfully")
    void findAllResolvesPageable() throws Exception {
        PageResponse<OrderResponse> page = new PageResponse<>(List.of(), new PageResponse.PageMetadata(10, 0, 0, 0));
        when(ordersClient.findAll(any(), any(), any(Pageable.class)))
                .thenReturn(StandardResponse.ok("Pedidos obtenidos exitosamente", page));

        mockMvc.perform(get("/api/v1/orders")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(ordersClient).findAll(eq(null), eq(null), pageableCaptor.capture());

        Pageable resolved = pageableCaptor.getValue();
        assertThat(resolved.getPageNumber()).isEqualTo(0);
        assertThat(resolved.getPageSize()).isEqualTo(10);
        assertThat(resolved.getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(resolved.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("GET /api/v1/orders/me with pagination parameters resolves Pageable successfully")
    void findMyOrdersResolvesPageable() throws Exception {
        PageResponse<OrderResponse> page = new PageResponse<>(List.of(), new PageResponse.PageMetadata(5, 1, 0, 0));
        when(ordersClient.findMyOrders(any(Pageable.class)))
                .thenReturn(StandardResponse.ok("Pedidos obtenidos exitosamente", page));

        mockMvc.perform(get("/api/v1/orders/me")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "createdAt,asc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(ordersClient).findMyOrders(pageableCaptor.capture());

        Pageable resolved = pageableCaptor.getValue();
        assertThat(resolved.getPageNumber()).isEqualTo(1);
        assertThat(resolved.getPageSize()).isEqualTo(5);
        assertThat(resolved.getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(resolved.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.ASC);
    }
}
