package cl.pedidos360.ms_orders.consumer;

import cl.pedidos360.ms_orders.config.RabbitMQConfig;
import cl.pedidos360.ms_orders.dto.events.OrderAcceptedEvent;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OrderAuditConsumer {

    @RabbitListener(queues = RabbitMQConfig.AUDIT_QUEUE)
    public void auditAcceptedOrder(
            OrderAcceptedEvent event,
            Message message,
            Channel channel
    ) throws IOException {
        try {
            validate(event);
            log.info("Order accepted audit | eventId={} | orderId={} | itemCount={}",
                    event.eventId(), event.orderId(), event.items().size());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (RuntimeException exception) {
            log.error("Audit event failed; retrying and then routing to DLQ | orderId={}",
                    event == null ? null : event.orderId(), exception);
            throw exception;
        }
    }

    private static void validate(OrderAcceptedEvent event) {
        if (event == null || event.eventId() == null || event.orderId() == null
                || event.items() == null || event.items().isEmpty()) {
            throw new IllegalArgumentException("Invalid order.accepted event for audit");
        }
    }
}
