
package cl.eventpass.bff.client;

import cl.eventpass.bff.dto.request.OrderCreateRequest;
import cl.eventpass.bff.dto.request.OrderStatusUpdateRequest;
import cl.eventpass.bff.dto.response.DashboardSummaryResponse;
import cl.eventpass.bff.dto.response.OrderResponse;
import cl.eventpass.bff.dto.response.PageResponse;
import cl.eventpass.bff.dto.response.StandardResponse;
import cl.eventpass.bff.enums.OrderStateEnum;
import cl.eventpass.bff.security.CurrentAuthorizationProvider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.util.Map;
import java.util.UUID;

@Component
public class OrdersClient {

    private static final String ORDERS_API_URI = "/api/v1/orders";

    private final RestClient ordersRestClient;
    private final CurrentAuthorizationProvider currentAuthorizationProvider;

    public OrdersClient(
            @Qualifier("ordersRestClient") RestClient ordersRestClient,
            CurrentAuthorizationProvider currentAuthorizationProvider
    ) {
        this.ordersRestClient = ordersRestClient;
        this.currentAuthorizationProvider = currentAuthorizationProvider;
    }

    // ======================
    // ÓRDENES
    // ======================

    public StandardResponse<OrderResponse> createOrder(
            OrderCreateRequest request
    ) {
        return ordersRestClient.post()
                .uri(ORDERS_API_URI)
                .headers(this::setAuthorization)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public StandardResponse<PageResponse<OrderResponse>> findAll(
            UUID customerId,
            OrderStateEnum status,
            Pageable pageable
    ) {
        return ordersRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(ORDERS_API_URI);

                    if (customerId != null) {
                        uriBuilder.queryParam("customerId", customerId);
                    }

                    if (status != null) {
                        uriBuilder.queryParam("status", status);
                    }

                    addPageableParams(uriBuilder, pageable);

                    return uriBuilder.build();
                })
                .headers(this::setAuthorization)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public StandardResponse<PageResponse<OrderResponse>> findMyOrders(
            Pageable pageable
    ) {
        return ordersRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(ORDERS_API_URI + "/me");
                    addPageableParams(uriBuilder, pageable);
                    return uriBuilder.build();
                })
                .headers(this::setAuthorization)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public StandardResponse<OrderResponse> findById(UUID orderId) {
        return ordersRestClient.get()
                .uri(ORDERS_API_URI + "/{orderId}", orderId)
                .headers(this::setAuthorization)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public StandardResponse<OrderResponse> cancel(UUID orderId) {
        return ordersRestClient.patch()
                .uri(ORDERS_API_URI + "/{orderId}/cancel", orderId)
                .headers(this::setAuthorization)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public StandardResponse<OrderResponse> updateStatus(
            UUID orderId,
            OrderStatusUpdateRequest request
    ) {
        return ordersRestClient.put()
                .uri(ORDERS_API_URI + "/{orderId}/status", orderId)
                .headers(this::setAuthorization)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public StandardResponse<OrderResponse> accept(UUID orderId) {
        return ordersRestClient.patch()
                .uri(ORDERS_API_URI + "/{orderId}/accept", orderId)
                .headers(this::setAuthorization)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public StandardResponse<DashboardSummaryResponse> getDashboardSummary() {
        return ordersRestClient.get()
                .uri(ORDERS_API_URI + "/dashboard/summary")
                .headers(this::setAuthorization)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    // ======================
    // MÉTODOS AUXILIARES
    // ======================

    private void setAuthorization(HttpHeaders headers) {
        String authorization =
                currentAuthorizationProvider.getAuthorization();

        headers.setBearerAuth(
                authorization.replaceFirst("^Bearer ", "")
        );
    }

    /**
     * Agrega los parámetros de paginación y ordenamiento a la URI.
     */
    private void addPageableParams(
            UriBuilder uriBuilder,
            Pageable pageable
    ) {
        if (pageable == null) {
            return;
        }

        uriBuilder.queryParam("page", pageable.getPageNumber());
        uriBuilder.queryParam("size", pageable.getPageSize());

        if (pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order ->
                    uriBuilder.queryParam(
                            "sort",
                            order.getProperty() + ","
                                    + order.getDirection().name().toLowerCase()
                    )
            );
        }
    }
}
