package cl.pedidos360.ms_catalog.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDERS_EXCHANGE = "orders.exchange";
    public static final String ROUTING_KEY = "order.created";
    public static final String CATALOG_QUEUE = "catalog.order-created.queue";

    public static final String DLX_EXCHANGE = "orders.dlx";
    public static final String DLQ_ROUTING_KEY = "order.created.dead";
    public static final String CATALOG_DLQ = "catalog.order-created.dlq";

    /**
     * Exchange principal para los eventos de pedidos.
     */
    @Bean
    public DirectExchange ordersExchange() {
        return new DirectExchange(ORDERS_EXCHANGE, true, false);
    }

    /**
     * Exchange para mensajes rechazados por fallos de procesamiento.
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    /**
     * Cola principal. Los mensajes rechazados se envían a la DLQ.
     */
    @Bean
    public Queue catalogOrderCreatedQueue() {
        return QueueBuilder.durable(CATALOG_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(DLQ_ROUTING_KEY)
                .build();
    }

    /**
     * Cola durable para los mensajes que no pudieron procesarse.
     */
    @Bean
    public Queue catalogOrderCreatedDlq() {
        return QueueBuilder.durable(CATALOG_DLQ).build();
    }

    /**
     * Vincula la cola principal al exchange de pedidos.
     */
    @Bean
    public Binding catalogOrderCreatedBinding(
            @Qualifier("catalogOrderCreatedQueue") Queue queue,
            @Qualifier("ordersExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(ROUTING_KEY);
    }

    /**
     * Vincula la DLQ al exchange de mensajes fallidos.
     */
    @Bean
    public Binding catalogOrderCreatedDlqBinding(
            @Qualifier("catalogOrderCreatedDlq") Queue queue,
            @Qualifier("deadLetterExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(DLQ_ROUTING_KEY);
    }

    /**
     * Conversor JSON para los eventos recibidos por RabbitMQ.
     */
    @Bean
    public JacksonJsonMessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
