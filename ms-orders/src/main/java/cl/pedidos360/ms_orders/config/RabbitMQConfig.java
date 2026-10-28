package cl.pedidos360.ms_orders.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange principal compartido por los microservicios
    public static final String ORDERS_EXCHANGE = "orders.exchange";
    public static final String ORDERS_ROUTING_KEY = "order.created";

    // Cola de notificaciones y su DLQ
    public static final String NOTIFICATION_QUEUE =
            "orders.notification.queue";
    public static final String NOTIFICATION_DLQ =
            "orders.notification.dlq";
    public static final String NOTIFICATION_DLK =
            "order.notification.dead";

    // Exchange común de mensajes fallidos
    public static final String DLX_EXCHANGE = "orders.dlx";

    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange("orders.exchange", true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument(
                        "x-dead-letter-routing-key",
                        NOTIFICATION_DLK
                )
                .build();
    }

    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(NOTIFICATION_DLQ).build();
    }

    @Bean
    public Binding notificationBinding(
            @Qualifier("notificationQueue") Queue queue,
            @Qualifier("ordersExchange") TopicExchange exchange
    ) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(ORDERS_ROUTING_KEY);
    }

    @Bean
    public Binding notificationDlqBinding(
            @Qualifier("notificationDlq") Queue queue,
            @Qualifier("deadLetterExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(NOTIFICATION_DLK);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        JacksonJsonMessageConverter converter =
                new JacksonJsonMessageConverter();

        converter.setTypePrecedence(
                JacksonJavaTypeMapper.TypePrecedence.INFERRED
        );

        return converter;
    }
}
