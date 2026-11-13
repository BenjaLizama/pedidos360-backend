package cl.pedidos360.bff.dto.request;

import java.util.UUID;

/**
 * Representa un producto y la cantidad que debe descontarse del inventario.
 */
public record BulkStockReductionItemRequest(
        UUID productId,
        Integer quantity
) {
}
