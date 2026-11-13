package cl.pedidos360.ms_catalog.service.impl;

import cl.pedidos360.ms_catalog.dto.events.OrderAcceptedEvent;
import cl.pedidos360.ms_catalog.dto.request.ProductStockReductionItemRequest;
import cl.pedidos360.ms_catalog.entity.ProcessedOrderEventEntity;
import cl.pedidos360.ms_catalog.repository.ProcessedOrderEventRepository;
import cl.pedidos360.ms_catalog.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Procesa el evento y registra su idempotencia dentro de una
 * única transacción MongoDB.
 */
@Service
@RequiredArgsConstructor
public class OrderEventTransactionalProcessor {

    private final ProcessedOrderEventRepository processedEventRepository;
    private final ProductService productService;

    /**
     * @return true si el evento se procesó; false si ya estaba registrado.
     */
    @Transactional(transactionManager = "transactionManager")
    public boolean process(
            OrderAcceptedEvent event,
            List<ProductStockReductionItemRequest> items
    ) {
        if (processedEventRepository.existsById(event.eventId())) {
            return false;
        }

        ProcessedOrderEventEntity processedEvent =
                new ProcessedOrderEventEntity(
                        event.eventId(),
                        event.orderId()
                );

        /*
         * El _id único evita que dos transacciones puedan registrar
         * exitosamente el mismo eventId.
         */
        processedEventRepository.insert(processedEvent);

        /*
         * ProductServiceImpl también usa @Transactional.
         * Al utilizar el mismo transaction manager, participa en
         * la transacción activa.
         */
        productService.bulkReduceStock(items);

        processedEvent.setProcessedAt(Instant.now());
        processedEventRepository.save(processedEvent);

        return true;
    }
}
