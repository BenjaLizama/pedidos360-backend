package cl.pedidos360.bff.service;

import cl.pedidos360.bff.dto.events.OrderEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderPublisherService {

    private static final String EXCHANGE = "orders.exchange";
    private static final String ROUTING_KEY = "order.created";

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderCreated(OrderEventDto event) {
        log.info(
                "[BFF] Publicando evento de orden creada. ID: {}",
                event.orderId()
        );

        rabbitTemplate.convertAndSend(
                EXCHANGE,
                ROUTING_KEY,
                event
        );
    }
}
