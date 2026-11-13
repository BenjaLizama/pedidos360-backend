package cl.pedidos360.ms_catalog.service.impl;

import cl.pedidos360.ms_catalog.dto.events.OrderAcceptedEvent;
import cl.pedidos360.ms_catalog.dto.request.ProductStockReductionItemRequest;
import cl.pedidos360.ms_catalog.repository.ProcessedOrderEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventProcessingService {

    private final OrderEventTransactionalProcessor transactionalProcessor;
    private final ProcessedOrderEventRepository processedEventRepository;

    public void process(
            OrderAcceptedEvent event,
            List<ProductStockReductionItemRequest> items
    ) {
        try {
            boolean processed = transactionalProcessor.process(event, items);

            if (!processed) {
                log.info(
                        "Evento duplicado ignorado | eventId={} | orderId={}",
                        event.eventId(),
                        event.orderId()
                );
            }
        } catch (DuplicateKeyException exception) {
            /*
             * Puede ocurrir si dos consumidores intentan registrar
             * simultáneamente el mismo evento.
             */
            if (processedEventRepository.existsById(event.eventId())) {
                log.info(
                        "Evento ya procesado por otra ejecución | eventId={} | orderId={}",
                        event.eventId(),
                        event.orderId()
                );
                return;
            }

            // Si no existe el registro confirmado, es un error real.
            throw exception;
        }
    }
}
