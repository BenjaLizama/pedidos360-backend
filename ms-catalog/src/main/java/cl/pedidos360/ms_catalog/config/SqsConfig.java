package cl.pedidos360.ms_catalog.config;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

/**
 * Configuración de integración con Amazon SQS.
 *
 * <p>Expone un {@link SqsTemplate} como bean de Spring para permitir
 * el envío y gestión de mensajes hacia las colas configuradas para
 * el microservicio.</p>
 *
 * <p>El cliente {@link SqsAsyncClient} es proporcionado por la
 * configuración de Spring Cloud AWS y utiliza las propiedades
 * definidas para el entorno correspondiente.</p>
 */
@Configuration
public class SqsConfig {

    /**
     * Crea la plantilla utilizada para interactuar con Amazon SQS.
     *
     * @param sqsAsyncClient cliente asíncrono de AWS SQS
     * @return instancia configurada de {@link SqsTemplate}
     */
    @Bean
    public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient) {
        return SqsTemplate.newTemplate(sqsAsyncClient);
    }
}
