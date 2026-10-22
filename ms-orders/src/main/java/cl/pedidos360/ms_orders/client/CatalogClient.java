package cl.pedidos360.ms_orders.client;

import cl.pedidos360.ms_orders.dto.response.ProductResponse;
import cl.pedidos360.ms_orders.dto.response.StandardResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 * Cliente HTTP encargado de la comunicación entre ms-orders y ms-catalog.
 *
 * <p>Permite consultar información de productos mediante el endpoint
 * de búsqueda por identificador de ms-catalog, utilizando Spring RestClient
 * para realizar las solicitudes HTTP.</p>
 *
 * <p>La URL base del microservicio de catálogo se obtiene desde la propiedad
 * {@code services.catalog.url}. Si no está configurada, se utiliza
 * {@code http://ms-catalog:8081} como valor predeterminado.</p>
 */
@Component
public class CatalogClient {

    /**
     * Cliente HTTP utilizado para realizar solicitudes a ms-catalog.
     */
    private final RestClient restClient;

    /**
     * Inicializa el cliente HTTP y configura la URL base del endpoint
     * de productos del microservicio de catálogo.
     *
     * @param restClientBuilder constructor de RestClient proporcionado por Spring.
     * @param catalogServiceUrl URL base del microservicio ms-catalog.
     */
    public CatalogClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.catalog.url:http://ms-catalog:8081}") String catalogServiceUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(catalogServiceUrl + "/api/v1/products")
                .build();
    }

    /**
     * Obtiene la información de un producto mediante su identificador único.
     *
     * <p>Realiza una solicitud HTTP GET al endpoint
     * {@code /api/v1/products/{productId}} de ms-catalog y procesa
     * la respuesta encapsulada en {@link StandardResponse}.</p>
     *
     * @param productId identificador único del producto que se desea consultar.
     * @return información del producto recuperado.
     * @throws IllegalStateException si la respuesta recibida es nula o no contiene
     *                               información del producto.
     * @throws org.springframework.web.client.RestClientException si ocurre un error
     *         durante la comunicación HTTP o al procesar una respuesta no exitosa.
     */
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
