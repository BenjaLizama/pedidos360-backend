package cl.pedidos360.ms_orders.service;

import cl.pedidos360.ms_orders.config.RabbitMQConfig;
import cl.pedidos360.ms_orders.dto.events.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPublisherService {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info(
                "Publicando order.created | eventId={} | orderId={}",
                event.eventId(),
                event.orderId()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDERS_EXCHANGE,
                RabbitMQConfig.ORDERS_ROUTING_KEY,
                event
        );
    }
}