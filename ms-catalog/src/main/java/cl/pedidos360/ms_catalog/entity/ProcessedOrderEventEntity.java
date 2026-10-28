package cl.pedidos360.ms_catalog.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

/**
 * Registra los eventos de pedidos procesados correctamente.
 *
 * El eventId es el identificador único del documento en MongoDB.
 * El registro se confirma en la misma transacción que la reducción
 * de stock, evitando procesar dos veces el mismo evento.
 */
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "processed_order_events")
public class ProcessedOrderEventEntity {

    @Id
    private UUID eventId;

    private UUID orderId;

    private Instant processedAt;

    public ProcessedOrderEventEntity(UUID eventId, UUID orderId) {
        this.eventId = eventId;
        this.orderId = orderId;
    }
}
