package cl.eventpass.bff.client;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import tools.jackson.databind.JsonNode;

@Component
public class OrdersClient {

    private static final String ORDERS_API_URI = "/api/v1/orders";

    private final RestClient ordersRestClient;

    public OrdersClient(
            @Qualifier("ordersRestClient") RestClient ordersRestClient
    ) {
        this.ordersRestClient = ordersRestClient;
    }

    // ======================
    //  ORDENES
    // ======================

    // ---- CREAR UNA ORDEN ----
    public JsonNode createOrder(Object request, String authorization) {
        return ordersRestClient.post()
                .uri(ORDERS_API_URI)
                .headers(headers -> applyAuthorization(headers, authorization))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JsonNode.class);
    }

    // ---- LISTAR ORDENES ----
    public JsonNode findAll(Map<String, ?> queryParams, String authorization) {
        return ordersRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(ORDERS_API_URI);
                    addQueryParams(uriBuilder, queryParams);
                    return uriBuilder.build();
                })
                .headers(headers -> applyAuthorization(headers, authorization))
                .retrieve()
                .body(JsonNode.class);
    }

    // ---- CONSULTAR ORDENES DEL USUARIO AUTENTICADO ----
    public JsonNode findMyOrders(Map<String, ?> queryParams, String authorization) {
        return ordersRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(ORDERS_API_URI + "/me");
                    addQueryParams(uriBuilder, queryParams);
                    return uriBuilder.build();
                })
                .headers(headers -> applyAuthorization(headers, authorization))
                .retrieve()
                .body(JsonNode.class);
    }

    // ---- OBTENER UNA ORDEN POR ID ----
    public JsonNode findById(UUID orderId, String authorization) {
        return ordersRestClient.get()
                .uri(ORDERS_API_URI + "/{orderId}", orderId)
                .headers(headers -> applyAuthorization(headers, authorization))
                .retrieve()
                .body(JsonNode.class);
    }

    // ---- CANCELAR UNA ORDEN ----
    public JsonNode cancel(UUID orderId, String authorization) {
        return ordersRestClient.patch()
                .uri(ORDERS_API_URI + "/{orderId}/cancel", orderId)
                .headers(headers -> applyAuthorization(headers, authorization))
                .retrieve()
                .body(JsonNode.class);
    }

    // ---- ACTUALIZAR ESTADO DE UNA ORDEN ----
    public JsonNode updateStatus(UUID orderId, Object request, String authorization) {
        return ordersRestClient.put()
                .uri(ORDERS_API_URI + "/{orderId}/status", orderId)
                .headers(headers -> applyAuthorization(headers, authorization))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JsonNode.class);
    }

    // ---- ACEPTAR UNA ORDEN ----
    public JsonNode accept(UUID orderId, String authorization) {
        return ordersRestClient.patch()
                .uri(ORDERS_API_URI + "/{orderId}/accept", orderId)
                .headers(headers -> applyAuthorization(headers, authorization))
                .retrieve()
                .body(JsonNode.class);
    }

    // ---- CONSULTAR RESUMEN DE ORDENES ----
    public JsonNode getDashboardSummary(String authorization) {
        return ordersRestClient.get()
                .uri(ORDERS_API_URI + "/dashboard/summary")
                .headers(headers -> applyAuthorization(headers, authorization))
                .retrieve()
                .body(JsonNode.class);
    }

    // ---- METODOS AUXILIARES ----

    private void applyAuthorization(
            HttpHeaders headers,
            String authorization
    ) {
        if (authorization != null && !authorization.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }

        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    }

    private void addQueryParams(
            UriBuilder uriBuilder,
            Map<String, ?> queryParams
    ) {
        if (queryParams == null || queryParams.isEmpty()) {
            return;
        }

        queryParams.forEach((key, value) -> {
            if (value != null) {
                uriBuilder.queryParam(key, value);
            }
        });
    }
}
