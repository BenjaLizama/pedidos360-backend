package cl.pedidos360.ms_orders.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuración de los clientes HTTP del microservicio ms-orders.
 *
 * <p>Registra los componentes de Spring necesarios para construir y utilizar
 * instancias de {@link RestClient}, facilitando la comunicación HTTP con otros
 * servicios de la arquitectura.</p>
 */
@Configuration
public class RestClientConfig {

    /**
     * Proporciona un constructor de RestClient administrado por Spring.
     *
     * <p>Este constructor puede utilizarse para crear clientes HTTP con
     * configuraciones específicas, como una URL base.</p>
     *
     * @return constructor de RestClient.
     */
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    /**
     * Registra una instancia de RestClient construida a partir del constructor
     * configurado en esta clase.
     *
     * @param builder constructor de RestClient proporcionado por Spring.
     * @return instancia de RestClient.
     */
    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }
}
