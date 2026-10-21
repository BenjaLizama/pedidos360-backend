package cl.pedidos360.ms_orders.client;

import cl.pedidos360.ms_orders.dto.response.ProductResponse;
import cl.pedidos360.ms_orders.dto.response.StandardResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class CatalogClient {

    private final RestClient restClient;

    public CatalogClient(RestClient.Builder restClientBuilder,
                         @Value("${services.catalog.url:http://ms-catalog:8081}") String catalogServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(catalogServiceUrl + "/api/v1/products")
                .build();
    }

    public ProductResponse findById(UUID productId) {
        StandardResponse<ProductResponse> response = restClient
                .get()
                .uri("/{productId}", productId)
                .retrieve()
                .body(new ParameterizedTypeReference<StandardResponse<ProductResponse>>() {});

        if (response == null || response.data() == null) {
            throw new IllegalStateException("No se pudo recuperar el producto " + productId);
        }

        return response.data();
    }
}
