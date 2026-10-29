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

    private static final int MAX_ATTEMPTS = 3;

    @RabbitListener(queues = RabbitMQConfig.AUDIT_QUEUE)
    public void auditAcceptedOrder(
            OrderAcceptedEvent event,
            Message message,
            Channel channel
    ) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                validate(event);
                log.info("Order accepted audit | eventId={} | orderId={} | itemCount={}",
                        event.eventId(), event.orderId(), event.items().size());
                channel.basicAck(deliveryTag, false);
                return;
            } catch (RuntimeException exception) {
                if (attempt == MAX_ATTEMPTS) {
                    log.error("Audit failed after {} attempts; routing to DLQ | orderId={}",
                            MAX_ATTEMPTS, event == null ? null : event.orderId(), exception);
                    channel.basicNack(deliveryTag, false, false);
                    return;
                }

                log.warn("Audit attempt {}/{} failed; retrying | orderId={}",
                        attempt, MAX_ATTEMPTS, event == null ? null : event.orderId(), exception);
                if (!waitBeforeRetry(attempt * 1000L)) {
                    log.warn("Audit retry interrupted; routing to DLQ | orderId={}",
                            event == null ? null : event.orderId());
                    channel.basicNack(deliveryTag, false, false);
                    return;
                }
            }
        }
    }

    private static boolean waitBeforeRetry(long delayMillis) {
        try {
            Thread.sleep(delayMillis);
            return true;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static void validate(OrderAcceptedEvent event) {
        if (event == null || event.eventId() == null || event.orderId() == null
                || event.items() == null || event.items().isEmpty()) {
            throw new IllegalArgumentException("Invalid order.accepted event for audit");
        }
    }
}
