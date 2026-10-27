package cl.pedidos360.ms_catalog.dto.response;

import cl.pedidos360.ms_catalog.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Respuesta principal utilizada para representar un producto del catálogo.
 *
 * <p>Expone la información comercial y operativa necesaria para consultar
 * un producto, incluyendo precio, stock, categoría y estado.</p>
 *
 * <p>Los campos de auditoría permiten conocer cuándo fue creado y cuándo
 * fue modificado por última vez, sin exponer directamente la entidad
 * persistente.</p>
 *
 * @param id identificador único del producto
 * @param sku código único utilizado para identificar el producto
 * @param name nombre del producto
 * @param description descripción del producto
 * @param price precio actual del producto
 * @param stock stock actual disponible
 * @param categoryId identificador de la categoría asociada
 * @param status estado actual del producto
 * @param createdAt fecha y hora de creación
 * @param updatedAt fecha y hora de la última modificación
 */
public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        UUID categoryId,
        ProductStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
