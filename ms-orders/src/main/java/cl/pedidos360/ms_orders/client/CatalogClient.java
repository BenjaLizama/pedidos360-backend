package cl.pedidos360.ms_orders.client;

import cl.pedidos360.ms_orders.dto.request.BulkStockReductionItemRequest;
import cl.pedidos360.ms_orders.dto.response.ProductResponse;
import cl.pedidos360.ms_orders.dto.response.StandardResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

/**
 * Cliente HTTP encargado de la comunicación entre ms-orders y ms-catalog.
 *
 * Propaga el token JWT del usuario autenticado para que ms-catalog
 * pueda aplicar sus propias reglas de autorización.
 */
@Component
public class CatalogClient {

    private final RestClient restClient;

    public CatalogClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.catalog.url:http://ms-catalog:8081}")
            String catalogServiceUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(catalogServiceUrl + "/api/v1/products")
                .build();
    }

    /**
     * Obtiene un producto por su identificador.
     */
    public ProductResponse findById(UUID productId) {
        StandardResponse<ProductResponse> response = restClient
                .get()
                .uri("/{productId}", productId)
                .headers(headers -> headers.setBearerAuth(getAccessToken()))
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                StandardResponse<ProductResponse>
                                >() {}
                );

        if (response == null || response.data() == null) {
            throw new IllegalStateException(
                    "No se pudo recuperar el producto " + productId
            );
        }

        return response.data();
    }

    /**
     * Solicita la reducción de stock de todos los productos de una orden.
     *
     * @param items productos y cantidades que deben descontarse.
     */
    public void reduceStock(List<BulkStockReductionItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                    "La lista de productos para reducir stock no puede estar vacía."
            );
        }

        restClient
                .post()
                .uri("/bulk/reduce-stock")
                .headers(headers -> headers.setBearerAuth(getAccessToken()))
                .body(items)
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * Obtiene el token JWT de la autenticación actual.
     */
    private String getAccessToken() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthentication
                && authentication.isAuthenticated()) {
            return jwtAuthentication.getToken().getTokenValue();
        }

        throw new IllegalStateException(
                "No se encontró un token JWT válido para comunicarse con ms-catalog."
        );
    }
}
