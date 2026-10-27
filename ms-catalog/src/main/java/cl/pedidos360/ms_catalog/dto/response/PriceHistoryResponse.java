package cl.pedidos360.ms_catalog.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Respuesta que representa un registro histórico de modificación
 * del precio de un producto.
 *
 * <p>Este DTO permite consultar la trazabilidad de los cambios de precio
 * sin exponer directamente la entidad de persistencia.</p>
 *
 * @param id identificador único del registro histórico
 * @param productId identificador del producto cuyo precio fue modificado
 * @param previousPrice precio anterior del producto
 * @param newPrice nuevo precio aplicado
 * @param reason motivo informado para el cambio
 * @param correlationId identificador utilizado para relacionar la operación
 * @param createdAt fecha y hora en que se registró el cambio
 * @param createdBy identificador del usuario que realizó el cambio
 */
public record PriceHistoryResponse(
        UUID id,
        UUID productId,
        BigDecimal previousPrice,
        BigDecimal newPrice,
        String reason,
        UUID correlationId,
        Instant createdAt,
        UUID createdBy
) {
}
