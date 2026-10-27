package cl.pedidos360.ms_catalog.mapper;

import cl.pedidos360.ms_catalog.dto.response.StockMovementResponse;
import cl.pedidos360.ms_catalog.entity.StockMovementEntity;
import cl.pedidos360.ms_catalog.enums.StockMovementType;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper responsable de transformar los movimientos de stock.
 * <p>
 * Los movimientos son registros generados por las operaciones
 * del sistema y no son creados directamente por el cliente.
 */
@Component
public class StockMovementMapper {

    /**
     * Crea una entidad de movimiento de stock.
     *
     * @param productId identificador del producto
     * @param type tipo de movimiento
     * @param quantity cantidad involucrada
     * @param previousStock stock anterior
     * @param resultingStock stock resultante
     * @param reason motivo del movimiento
     * @param correlationId identificador de trazabilidad
     * @return entidad de movimiento de stock
     */
    public StockMovementEntity toEntity(
            UUID productId,
            StockMovementType type,
            Integer quantity,
            Integer previousStock,
            Integer resultingStock,
            String reason,
            UUID correlationId
    ) {
        StockMovementEntity movement = new StockMovementEntity();

        movement.setProductId(productId);
        movement.setType(type);
        movement.setQuantity(quantity);
        movement.setPreviousStock(previousStock);
        movement.setResultingStock(resultingStock);
        movement.setReason(reason);
        movement.setCorrelationId(correlationId);

        return movement;
    }

    /**
     * Convierte una entidad de movimiento de stock
     * en su DTO de respuesta.
     */
    public StockMovementResponse toResponse(
            StockMovementEntity stockMovement
    ) {

        return new StockMovementResponse(
                stockMovement.getId(),
                stockMovement.getProductId(),
                stockMovement.getType(),
                stockMovement.getQuantity(),
                stockMovement.getPreviousStock(),
                stockMovement.getResultingStock(),
                stockMovement.getReason(),
                stockMovement.getCorrelationId(),
                stockMovement.getCreatedAt(),
                stockMovement.getCreatedBy()
        );
    }
}
