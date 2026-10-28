package cl.pedidos360.ms_orders.consumer;

import cl.pedidos360.ms_orders.config.RabbitMQConfig;
import cl.pedidos360.ms_orders.dto.events.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationConsumer {

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleOrderCreatedNotification(OrderCreatedEvent event) {
        log.info("[NOTIFICACIÓN] Generando ticket/comprobante para la Orden ID: {}", event.orderId());
        log.info("[NOTIFICACIÓN] Ticket procesado con éxito para {} items de la orden {}",
                event.items().size(), event.orderId());
    }
}