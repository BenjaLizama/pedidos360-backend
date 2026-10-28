package cl.pedidos360.bff.client;

import cl.pedidos360.bff.dto.request.BulkStockReductionItemRequest;
import cl.pedidos360.bff.dto.request.CategoryCreateRequest;
import cl.pedidos360.bff.dto.request.CategoryStatusUpdateRequest;
import cl.pedidos360.bff.dto.request.CategoryUpdateRequest;
import cl.pedidos360.bff.dto.request.ProductCreateRequest;
import cl.pedidos360.bff.dto.request.ProductPriceUpdateRequest;
import cl.pedidos360.bff.dto.request.ProductRestockRequest;
import cl.pedidos360.bff.dto.request.ProductStatusUpdateRequest;
import cl.pedidos360.bff.dto.request.ProductStockUpdateRequest;
import cl.pedidos360.bff.dto.request.ProductUpdateRequest;
import cl.eventpass.bff.dto.response.*;
import cl.pedidos360.bff.dto.response.*;
import cl.pedidos360.bff.security.CurrentAuthorizationProvider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class CatalogClient {

    private static final String PRODUCTS_API_URI = "/api/v1/products";
    private static final String CATEGORIES_API_URI = "/api/v1/categories";
    private static final String PRICE_HISTORY_API_URI = "/api/v1/price-history";
    private static final String STOCK_MOVEMENT_API_URI = "/api/v1/stock-movement";

    private final RestClient catalogRestClient;
    private final CurrentAuthorizationProvider currentAuthorizationProvider;

    public CatalogClient(
            @Qualifier("catalogRestClient") RestClient catalogRestClient,
            CurrentAuthorizationProvider currentAuthorizationProvider
    ) {
        this.catalogRestClient = catalogRestClient;
        this.currentAuthorizationProvider = currentAuthorizationProvider;
    }

    // -------------------------------------------------------------------------
    // Productos
    // -------------------------------------------------------------------------

    public StandardResponse<ProductResponse> createProduct(
            ProductCreateRequest request
    ) {
        return catalogRestClient.post()
                .uri(PRODUCTS_API_URI)
                .headers(this::setAuthorization)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public StandardResponse<ProductResponse> getProductById(UUID productId) {
        return catalogRestClient.get()
                .uri(PRODUCTS_API_URI + "/{productId}", productId)
                .headers(this::setAuthorization)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public StandardResponse<PageResponse<ProductResponse>> search(
            Map<String, ?> queryParams
    ) {
        return catalogRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(PRODUCTS_API_URI + "/search");
                    addQueryParams(uriBuilder, queryParams);
                    return uriBuilder.build();
                })
                .headers(this::setAuthorization)
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                StandardResponse<PageResponse<ProductResponse>>
                                >() {}
                );
    }

    public StandardResponse<ProductResponse> updateProduct(
            UUID productId,
            ProductUpdateRequest request
    ) {
        return catalogRestClient.put()
                .uri(PRODUCTS_API_URI + "/{productId}", productId)
                .headers(this::setAuthorization)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public StandardResponse<ProductResponse> updateProductStatus(
            UUID productId,
            ProductStatusUpdateRequest request
    ) {
        return catalogRestClient.patch()
                .uri(PRODUCTS_API_URI + "/{productId}/status", productId)
                .headers(this::setAuthorization)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public StandardResponse<ProductResponse> updateProductPrice(
            UUID productId,
            ProductPriceUpdateRequest request
    ) {
        return catalogRestClient.patch()
                .uri(PRODUCTS_API_URI + "/{productId}/price", productId)
                .headers(this::setAuthorization)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public StandardResponse<ProductResponse> updateProductStock(
            UUID productId,
            ProductStockUpdateRequest request
    ) {
        return catalogRestClient.patch()
                .uri(PRODUCTS_API_URI + "/{productId}/stock", productId)
                .headers(this::setAuthorization)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public StandardResponse<ProductResponse> restockProduct(
            UUID productId,
            ProductRestockRequest request
    ) {
        return catalogRestClient.patch()
                .uri(PRODUCTS_API_URI + "/{productId}/restock", productId)
                .headers(this::setAuthorization)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public void deleteProduct(UUID productId) {
        catalogRestClient.delete()
                .uri(PRODUCTS_API_URI + "/{productId}", productId)
                .headers(this::setAuthorization)
                .retrieve()
                .toBodilessEntity();
    }

    public StandardResponse<?> bulkReduceStock(
            List<BulkStockReductionItemRequest> request
    ) {
        return catalogRestClient.post()
                .uri(PRODUCTS_API_URI + "/bulk/reduce-stock")
                .headers(this::setAuthorization)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    // -------------------------------------------------------------------------
    // Categorías
    // -------------------------------------------------------------------------

    public StandardResponse<CategoryResponse> createCategory(
            CategoryCreateRequest request
    ) {
        return catalogRestClient.post()
                .uri(CATEGORIES_API_URI)
                .headers(this::setAuthorization)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public StandardResponse<?> getCategories(
            Map<String, ?> queryParams
    ) {
        return catalogRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(CATEGORIES_API_URI);
                    addQueryParams(uriBuilder, queryParams);
                    return uriBuilder.build();
                })
                .headers(this::setAuthorization)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public StandardResponse<CategoryResponse> getCategoryById(
            UUID categoryId
    ) {
        return catalogRestClient.get()
                .uri(CATEGORIES_API_URI + "/{categoryId}", categoryId)
                .headers(this::setAuthorization)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public StandardResponse<CategoryResponse> updateCategory(
            UUID categoryId,
            CategoryUpdateRequest request
    ) {
        return catalogRestClient.put()
                .uri(CATEGORIES_API_URI + "/{categoryId}", categoryId)
                .headers(this::setAuthorization)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public StandardResponse<CategoryResponse> updateCategoryStatus(
            UUID categoryId,
            CategoryStatusUpdateRequest request
    ) {
        return catalogRestClient.patch()
                .uri(CATEGORIES_API_URI + "/{categoryId}/status", categoryId)
                .headers(this::setAuthorization)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public void deleteCategory(UUID categoryId) {
        catalogRestClient.delete()
                .uri(CATEGORIES_API_URI + "/{categoryId}", categoryId)
                .headers(this::setAuthorization)
                .retrieve()
                .toBodilessEntity();
    }

    // -------------------------------------------------------------------------
    // Historial de precios
    // -------------------------------------------------------------------------

    public StandardResponse<PageResponse<PriceHistoryResponse>> getPriceHistory(
            UUID productId,
            Map<String, ?> queryParams
    ) {
        return catalogRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(PRICE_HISTORY_API_URI + "/{productId}");
                    addQueryParams(uriBuilder, queryParams);
                    return uriBuilder.build(productId);
                })
                .headers(this::setAuthorization)
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                StandardResponse<PageResponse<PriceHistoryResponse>>
                                >() {}
                );
    }

    // -------------------------------------------------------------------------
    // Movimientos de stock
    // -------------------------------------------------------------------------

    public StandardResponse<PageResponse<StockMovementResponse>> getStockMovements(
            UUID productId,
            Map<String, ?> queryParams
    ) {
        return catalogRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(STOCK_MOVEMENT_API_URI + "/{productId}");
                    addQueryParams(uriBuilder, queryParams);
                    return uriBuilder.build(productId);
                })
                .headers(this::setAuthorization)
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                StandardResponse<PageResponse<StockMovementResponse>>
                                >() {}
                );
    }

    // -------------------------------------------------------------------------
    // Métodos auxiliares
    // -------------------------------------------------------------------------

    private void setAuthorization(HttpHeaders headers) {
        String authorization =
                currentAuthorizationProvider.getAuthorization();

        if (authorization == null || authorization.isBlank()) {
            headers.remove(HttpHeaders.AUTHORIZATION);
            return;
        }

        headers.set(HttpHeaders.AUTHORIZATION, authorization);
    }

    /**
     * Agrega los parámetros de consulta, incluidos valores repetidos.
     */
    private void addQueryParams(
            UriBuilder uriBuilder,
            Map<String, ?> queryParams
    ) {
        if (queryParams == null || queryParams.isEmpty()) {
            return;
        }

        queryParams.forEach((key, value) -> {
            if (value instanceof Iterable<?> values) {
                values.forEach(item -> {
                    if (item != null) {
                        uriBuilder.queryParam(key, item);
                    }
                });
            } else if (value != null) {
                uriBuilder.queryParam(key, value);
            }
        });
    }
}
