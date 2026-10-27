package cl.pedidos360.ms_catalog.mapper;

import cl.pedidos360.ms_catalog.dto.response.PriceHistoryResponse;
import cl.pedidos360.ms_catalog.entity.PriceHistoryEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mapper responsable de transformar el historial de precios.
 * <p>
 * El historial es generado internamente por el sistema,
 * por lo que no necesitamos un Request DTO para su creación.
 */
@Component
public class PriceHistoryMapper {

    /**
     * Crea una entidad de historial de precios.
     */
    public PriceHistoryEntity toEntity(
            UUID productId,
            BigDecimal previousPrice,
            BigDecimal newPrice,
            String reason,
            UUID correlationId
    ) {
        PriceHistoryEntity history =
                new PriceHistoryEntity();

        history.setProductId(productId);
        history.setPreviousPrice(previousPrice);
        history.setNewPrice(newPrice);
        history.setReason(reason);
        history.setCorrelationId(correlationId);

        return history;
    }

    /**
     * Convierte una entidad de historial de precios
     * en su DTO de respuesta.
     */
    public PriceHistoryResponse toResponse(
            PriceHistoryEntity priceHistory
    ) {

        return new PriceHistoryResponse(
                priceHistory.getId(),
                priceHistory.getProductId(),
                priceHistory.getPreviousPrice(),
                priceHistory.getNewPrice(),
                priceHistory.getReason(),
                priceHistory.getCorrelationId(),
                priceHistory.getCreatedAt(),
                priceHistory.getCreatedBy()
        );
    }
}
