package cl.pedidos360.ms_catalog.consumer;

import cl.pedidos360.ms_catalog.config.RabbitMQConfig;
import cl.pedidos360.ms_catalog.dto.events.OrderCreatedEvent;
import cl.pedidos360.ms_catalog.dto.request.ProductStockReductionItemRequest;
import cl.pedidos360.ms_catalog.service.impl.OrderEventProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final OrderEventProcessingService orderEventProcessingService;

    @RabbitListener(queues = RabbitMQConfig.MAIN_QUEUE)
    public void consume(OrderCreatedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "El evento order.created recibido es nulo."
            );
        }

        if (event.eventId() == null) {
            throw new IllegalArgumentException(
                    "El evento order.created no contiene eventId."
            );
        }

        if (event.orderId() == null) {
            throw new IllegalArgumentException(
                    "El evento order.created no contiene orderId."
            );
        }

        if (event.items() == null || event.items().isEmpty()) {
            throw new IllegalArgumentException(
                    "El evento order.created no contiene productos."
            );
        }

        log.info(
                "Evento order.created recibido | eventId={} | orderId={}",
                event.eventId(),
                event.orderId()
        );

        List<ProductStockReductionItemRequest> items =
                event.items().stream()
                        .map(item -> {
                            if (item == null) {
                                throw new IllegalArgumentException(
                                        "El evento contiene un producto nulo."
                                );
                            }

                            if (item.productId() == null) {
                                throw new IllegalArgumentException(
                                        "El evento contiene un productId nulo."
                                );
                            }

                            if (item.quantity() <= 0) {
                                throw new IllegalArgumentException(
                                        "La cantidad debe ser mayor que cero."
                                );
                            }

                            return new ProductStockReductionItemRequest(
                                    item.productId(),
                                    item.quantity()
                            );
                        })
                        .toList();

        orderEventProcessingService.process(event, items);

        log.info(
                "Procesamiento de order.created finalizado | eventId={} | orderId={}",
                event.eventId(),
                event.orderId()
        );
    }
}
