package cl.eventpass.bff.client;

import cl.eventpass.bff.dto.response.OrderResponse;
import cl.eventpass.bff.dto.response.PageResponse;
import cl.eventpass.bff.dto.response.StandardResponse;
import cl.eventpass.bff.security.CurrentAuthorizationProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OrdersClientTest {

    private static final String PAGE_JSON = """
            {
              "status": 200,
              "message": "Órdenes consultadas con éxito.",
              "data": {
                "content": [],
                "page": {
                  "size": 5,
                  "number": 0,
                  "totalElements": 0,
                  "totalPages": 0
                }
              }
            }
            """;

    private MockRestServiceServer server;
    private OrdersClient ordersClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://orders.test");
        server = MockRestServiceServer.bindTo(builder).build();

        CurrentAuthorizationProvider authorizationProvider = mock(CurrentAuthorizationProvider.class);
        when(authorizationProvider.getAuthorization()).thenReturn("Bearer test-token");

        ordersClient = new OrdersClient(builder.build(), authorizationProvider);
    }

    @Test
    @DisplayName("findAll deserializes the orders page into PageResponse wrapped in StandardResponse")
    void findAllDeserializesPage() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith("http://orders.test/api/v1/orders?")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("page", "0"))
                .andExpect(queryParam("size", "5"))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andRespond(withSuccess(PAGE_JSON, MediaType.APPLICATION_JSON));

        StandardResponse<PageResponse<OrderResponse>> response = ordersClient.findAll(
                null,
                null,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.data()).isNotNull();
        assertThat(response.data().content()).isEmpty();
        assertThat(response.data().page().size()).isEqualTo(5);
        assertThat(response.data().page().number()).isZero();
        assertThat(response.data().page().totalElements()).isZero();
        assertThat(response.data().page().totalPages()).isZero();
        server.verify();
    }

    @Test
    @DisplayName("findMyOrders deserializes the orders page into PageResponse wrapped in StandardResponse")
    void findMyOrdersDeserializesPage() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith("http://orders.test/api/v1/orders/me?")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(PAGE_JSON, MediaType.APPLICATION_JSON));

        StandardResponse<PageResponse<OrderResponse>> response =
                ordersClient.findMyOrders(PageRequest.of(0, 5));

        assertThat(response.data()).isNotNull();
        assertThat(response.data().page().size()).isEqualTo(5);
        server.verify();
    }
}
