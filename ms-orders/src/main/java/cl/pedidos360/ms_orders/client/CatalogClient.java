package cl.pedidos360.ms_orders.client;

import cl.pedidos360.ms_orders.dto.response.ProductResponse;
import cl.pedidos360.ms_orders.dto.response.StandardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CatalogClient {

    private final RestClient restClient;

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
