package cl.eventpass.bff.client;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;

@Component
public class CatalogClient {

    private final RestClient catalogRestClient;

    public CatalogClient(
            @Qualifier("catalogRestClient") RestClient catalogRestClient
    ) {
        this.catalogRestClient = catalogRestClient;
    }

    private static final String PRODUCTS_API_URI = "/api/v1/products";
    private static final String CATEGORIES_API_URI = "/api/v1/categories";
    private static final String PRICE_HISTORY_API_URI = "/api/v1/price-history";
    private static final String STOCK_MOVEMENT_API_URI = "/api/v1/stock-movement";

    // ======================
    //  PRODUCTOS
    // ======================

    // ---- CREAR PRODUCTO ----
    public JsonNode createProduct(Object request, String authorization) {
        return catalogRestClient.post()
                .uri(PRODUCTS_API_URI)
                .headers(headers -> applyAuthorization(headers, authorization))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JsonNode.class);
    }

    // ---- OBTENER PRODUCTO POR ID ----
    public JsonNode getProductById(UUID productId, String authorization) {
        return catalogRestClient.get()
                .uri(PRODUCTS_API_URI + "/{productId}", productId)
                .headers(headers -> applyAuthorization(headers, authorization))
                .retrieve()
                .body(JsonNode.class);
    }

    // ---- BUSCAR PRODUCTOS ----
    public JsonNode search(Map<String, ?> queryParams, String authorization) {
        return catalogRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(PRODUCTS_API_URI + "/search");
                    addQueryParams(uriBuilder, queryParams);
                    return uriBuilder.build();
                })
                .headers(headers -> applyAuthorization(headers, authorization))
                .retrieve()
                .body(JsonNode.class);
    }

    // ---- ACTUALIZAR PRODUCTO ----
    public JsonNode updateProduct(UUID productId, Object request, String authorization) {
        return catalogRestClient.put()
                .uri(PRODUCTS_API_URI + "/{productId}", productId)
                .headers(headers -> applyAuthorization(headers, authorization))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JsonNode.class);
    }

    // ---- ACTUALIZAR ESTADO DEL PRODUCTO ----
    public JsonNode updateProductStatus(UUID productId, Object request, String authorization) {
        return catalogRestClient.patch()
                .uri(PRODUCTS_API_URI + "/{productId}/status", productId)
                .headers(headers -> applyAuthorization(headers, authorization))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JsonNode.class);
    }

    // ---- ACTUALIZAR PRECIO DEL PRODUCTO ----
    public JsonNode updateProductPrice(UUID productId, Object request, String authorization) {
        return catalogRestClient.patch()
                .uri(PRODUCTS_API_URI + "/{productId}/price", productId)
                .headers(headers -> applyAuthorization(headers, authorization))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JsonNode.class);
    }

    // ---- ACTUALIZAR STOCK DEL PRODUCTO ----
    public JsonNode updateProductStock(UUID productId, Object request, String authorization) {
        return catalogRestClient.patch()
                .uri(PRODUCTS_API_URI + "/{productId}/stock", productId)
                .headers(headers -> applyAuthorization(headers, authorization))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JsonNode.class);
    }

    // ---- AUMENTAR STOCK DEL PRODUCTO ----
    public JsonNode restockProduct(UUID productId, Object request, String authorization) {
        return catalogRestClient.patch()
                .uri(PRODUCTS_API_URI + "/{productId}/restock", productId)
                .headers(headers -> applyAuthorization(headers, authorization))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JsonNode.class);
    }

    // ---- ELIMINAR PRODUCTO ----
    public void deleteProduct(UUID productId, String authorization) {
        catalogRestClient.delete()
                .uri(PRODUCTS_API_URI + "/{productId}", productId)
                .headers(headers -> applyAuthorization(headers, authorization))
                .retrieve()
                .toBodilessEntity();
    }

    // ---- REDUCCION MASIVA DE STOCK ----
    public JsonNode bulkReduceStock(List<?> request, String authorization) {
        return catalogRestClient.post()
                .uri(PRODUCTS_API_URI + "/bulk/reduce-stock")
                .headers(headers -> applyAuthorization(headers, authorization))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JsonNode.class);
    }

    // ======================
    //  CATEGORIAS
    // ======================

    // ---- CREAR CATEGORIA ----
    public JsonNode createCategory(Object request, String authorization) {
        return catalogRestClient.post()
                .uri(CATEGORIES_API_URI)
                .headers(headers -> applyAuthorization(headers, authorization))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JsonNode.class);
    }

    // ---- OBTENER CATEGORIAS ----
    public JsonNode getCategories(Map<String, ?> queryParams, String authorization) {
        return catalogRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(CATEGORIES_API_URI);
                    addQueryParams(uriBuilder, queryParams);
                    return uriBuilder.build();
                })
                .headers(headers -> applyAuthorization(headers, authorization))
                .retrieve()
                .body(JsonNode.class);
    }

    // ---- OBTENER CATEGORIA POR ID ----
    public JsonNode getCategoryById(UUID categoryId, String authorization) {
        return catalogRestClient.get()
                .uri(CATEGORIES_API_URI + "/{categoryId}", categoryId)
                .headers(headers -> applyAuthorization(headers, authorization))
                .retrieve()
                .body(JsonNode.class);
    }

    // ---- ACTUALIZAR CATEGORIA ----
    public JsonNode updateCategory(UUID categoryId, Object request, String authorization) {
        return catalogRestClient.put()
                .uri(CATEGORIES_API_URI + "/{categoryId}", categoryId)
                .headers(headers -> applyAuthorization(headers, authorization))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JsonNode.class);
    }

    // ---- ACTUALIZAR ESTADO DE CATEGORIA ----
    public JsonNode updateCategoryStatus(UUID categoryId, Object request, String authorization) {
        return catalogRestClient.patch()
                .uri(CATEGORIES_API_URI + "/{categoryId}", categoryId)
                .headers(headers -> applyAuthorization(headers, authorization))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JsonNode.class);
    }

    // ---- ELIMINAR CATEGORIA ----
    public void deleteCategory(UUID categoryId, String authorization) {
        catalogRestClient.delete()
                .uri(CATEGORIES_API_URI + "/{categoryId}", categoryId)
                .headers(headers -> applyAuthorization(headers, authorization))
                .retrieve()
                .toBodilessEntity();
    }

    // ======================
    //  HISTORIAL DE PRECIOS
    // ======================

    // ---- OBTENER HISTORIAL DE PRECIOS ----
    public JsonNode getPriceHistory(UUID productId, Map<String, ?> queryParams, String authorization) {
        return catalogRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(PRICE_HISTORY_API_URI + "/{productId}");
                    addQueryParams(uriBuilder, queryParams);
                    return uriBuilder.build(productId);
                })
                .headers(headers -> applyAuthorization(headers, authorization))
                .retrieve()
                .body(JsonNode.class);
    }

    // ======================
    //  MOVIMIENTOS DE STOCK
    // ======================

    // ---- OBTENER MOVIMIENTOS DE STOCK DE UN PRODUCTO ----
    public JsonNode getStockMovements(UUID productId, Map<String, ?> queryParams, String authorization) {
        return catalogRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(STOCK_MOVEMENT_API_URI + "/{productId}");
                    addQueryParams(uriBuilder, queryParams);
                    return uriBuilder.build(productId);
                })
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
            org.springframework.web.util.UriBuilder uriBuilder,
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
