package cl.pedidos360.ms_catalog.consumer;

import cl.pedidos360.ms_catalog.config.RabbitMQConfig;
import cl.pedidos360.ms_catalog.dto.events.OrderCreatedEvent;
import cl.pedidos360.ms_catalog.dto.request.ProductStockReductionItemRequest;
import cl.pedidos360.ms_catalog.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final ProductService productService;

    @RabbitListener(queues = RabbitMQConfig.CATALOG_QUEUE)
    public void consume(OrderCreatedEvent event) {
        log.info(
                "Evento order.created recibido | eventId={} | orderId={}",
                event.eventId(),
                event.orderId()
        );

        List<ProductStockReductionItemRequest> items = event.items()
                .stream()
                .map(item -> new ProductStockReductionItemRequest(
                        item.productId(),
                        item.quantity()
                ))
                .toList();

        productService.bulkReduceStock(items);

        log.info(
                "Stock reducido correctamente | eventId={} | orderId={}",
                event.eventId(),
                event.orderId()
        );
    }
}