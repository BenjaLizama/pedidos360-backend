package cl.pedidos360.ms_catalog.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Nombres de Exchange, Queue y Routing Key Principales
    public static final String MAIN_EXCHANGE = "orders.exchange";
    public static final String MAIN_QUEUE = "catalog.order-created.queue";
    public static final String ROUTING_KEY = "order.created";

    // Nombres de DLX, DLQ y Routing Key de Mensajes Fallidos
    public static final String DLX_EXCHANGE = "orders.dlx";
    public static final String DLQ_QUEUE = "catalog.order-created.dlq";
    public static final String DLQ_ROUTING_KEY = "order.created.dead";

    // =========================================================================
    // CONFIGURACIÓN DE COLA PRINCIPAL
    // =========================================================================

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }

    @Bean
    public Queue mainQueue() {
        return QueueBuilder.durable(MAIN_QUEUE)
                // Redirige mensajes rechazados o fallidos hacia la DLX
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public TopicExchange mainExchange() {
        return new TopicExchange(MAIN_EXCHANGE);
    }

    @Bean
    public Binding mainBinding(Queue mainQueue, TopicExchange mainExchange) {
        return BindingBuilder.bind(mainQueue).to(mainExchange).with(ROUTING_KEY);
    }

    // =========================================================================
    // CONFIGURACIÓN DE DEAD LETTER EXCHANGE (DLX) Y DEAD LETTER QUEUE (DLQ)
    // =========================================================================

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_QUEUE).build();
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DLQ_ROUTING_KEY);
    }
}