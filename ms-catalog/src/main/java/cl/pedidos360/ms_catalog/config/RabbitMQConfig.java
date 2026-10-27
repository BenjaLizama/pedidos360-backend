package cl.pedidos360.ms_catalog.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDERS_EXCHANGE = "orders.exchange";
    public static final String ROUTING_KEY = "order.created";
    public static final String CATALOG_QUEUE = "catalog.order-created.queue";

    @Bean
    public DirectExchange ordersExchange() {
        return new DirectExchange(ORDERS_EXCHANGE, true, false);
    }

    @Bean
    public Queue catalogOrderCreatedQueue() {
        return QueueBuilder.durable(CATALOG_QUEUE).build();
    }

    @Bean
    public Binding catalogOrderCreatedBinding(
            Queue catalogOrderCreatedQueue,
            DirectExchange ordersExchange
    ) {
        return BindingBuilder
                .bind(catalogOrderCreatedQueue)
                .to(ordersExchange)
                .with(ROUTING_KEY);
    }

    @Bean
    public JacksonJsonMessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}