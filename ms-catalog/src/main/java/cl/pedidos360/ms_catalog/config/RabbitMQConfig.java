package cl.pedidos360.ms_catalog.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange principal compartido
    public static final String MAIN_EXCHANGE = "orders.exchange";
    public static final String ROUTING_KEY = "order.accepted";

    // Cola de catálogo y su DLQ
    public static final String MAIN_QUEUE =
            "catalog.order-created.queue";
    public static final String DLQ_QUEUE =
            "catalog.order-created.dlq";
    public static final String DLQ_ROUTING_KEY =
            "order.accepted.dead";

    // Exchange común de mensajes fallidos
    public static final String DLX_EXCHANGE = "orders.dlx";

    @Bean
    public TopicExchange mainExchange() {
        return new TopicExchange(MAIN_EXCHANGE, true, false);
    }

    @Bean
    public Binding mainBinding(
            @Qualifier("mainQueue") Queue queue,
            @Qualifier("mainExchange") TopicExchange exchange
    ) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(ROUTING_KEY);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue mainQueue() {
        return QueueBuilder.durable(MAIN_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument(
                        "x-dead-letter-routing-key",
                        DLQ_ROUTING_KEY
                )
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_QUEUE).build();
    }

    @Bean
    public Binding deadLetterBinding(
            @Qualifier("deadLetterQueue") Queue queue,
            @Qualifier("deadLetterExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(DLQ_ROUTING_KEY);
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
