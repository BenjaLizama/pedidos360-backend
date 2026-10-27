package cl.pedidos360.ms_catalog.dto.response;

import cl.pedidos360.ms_catalog.enums.StockMovementType;

import java.time.Instant;
import java.util.UUID;

/**
 * Respuesta que representa un movimiento de inventario de un producto.
 *
 * <p>Permite consultar la trazabilidad de las operaciones que modificaron
 * el stock, mostrando tanto el valor anterior como el resultado de la
 * operación.</p>
 *
 * @param id identificador único del movimiento
 * @param productId identificador del producto afectado
 * @param type tipo de movimiento realizado
 * @param quantity cantidad de unidades involucradas
 * @param previousStock stock existente antes de la operación
 * @param resultingStock stock resultante después de la operación
 * @param reason motivo asociado al movimiento
 * @param correlationId identificador utilizado para relacionar la operación
 * @param createdAt fecha y hora en que se registró el movimiento
 * @param createdBy identificador del usuario responsable del movimiento
 */
public record StockMovementResponse(
        UUID id,
        UUID productId,
        StockMovementType type,
        Integer quantity,
        Integer previousStock,
        Integer resultingStock,
        String reason,
        UUID correlationId,
        Instant createdAt,
        UUID createdBy
) {
}
