package cl.pedidos360.ms_orders.consumer;


import cl.pedidos360.ms_orders.config.RabbitMQConfig;
import cl.pedidos360.ms_orders.dto.events.OrderEventDto;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;

@Service
@Slf4j
public class OrderConsumer {

    @RabbitListener(queues = RabbitMQConfig.ORDERS_QUEUE)
    public void processOrder(
            OrderEventDto orderEvent,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        try {
            log.info(
                    "[MS-ORDERS] Procesando orden ID: {}",
                    orderEvent.orderId()
            );

            // Lógica de negocio: persistencia, validación de pago, etc.
            executeOrderProcessing(orderEvent);

            // Confirmación manual tras procesar correctamente.
            channel.basicAck(deliveryTag, false);

            log.info(
                    "[MS-ORDERS] ACK enviado correctamente para la orden ID: {}",
                    orderEvent.orderId()
            );

        } catch (Exception e) {
            log.error(
                    "[MS-ORDERS] Error procesando la orden ID: {}. Enviando a DLQ...",
                    orderEvent.orderId(),
                    e
            );

            // Rechaza el mensaje sin reencolarlo.
            // RabbitMQ lo enviará al DLX si la cola está configurada correctamente.
            channel.basicNack(deliveryTag, false, false);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.DLQ_QUEUE)
    public void processDLQ(OrderEventDto failedOrder) {
        log.warn(
                "[DLQ AUDITORÍA] Orden fallida aislada en cuarentena: ID {}",
                failedOrder.orderId()
        );
    }

    private void executeOrderProcessing(OrderEventDto order) {
        // Simulación de validación.
        if (order.amount() == null
                || order.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El monto del pedido debe ser mayor a cero"
            );
        }
    }
}