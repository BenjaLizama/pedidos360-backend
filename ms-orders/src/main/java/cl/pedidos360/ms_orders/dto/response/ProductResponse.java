package cl.pedidos360.ms_orders.dto.response;

import cl.pedidos360.ms_orders.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO de respuesta utilizado para representar un producto del catálogo.
 *
 * <p>Expone la información comercial y operativa necesaria para consultar
 * un producto, incluyendo su código, nombre, descripción, precio, stock,
 * categoría y estado actual.</p>
 *
 * <p>Este DTO permite a ms-orders recibir la información del producto
 * proporcionada por ms-catalog sin utilizar directamente la entidad
 * persistente del catálogo.</p>
 *
 * <p>Los campos de auditoría permiten conocer cuándo fue creado y cuándo
 * fue modificado por última vez.</p>
 *
 * @param id identificador único del producto.
 * @param sku código único utilizado para identificar el producto.
 * @param name nombre del producto.
 * @param description descripción del producto.
 * @param price precio actual del producto.
 * @param stock cantidad de unidades registradas en el inventario.
 * @param categoryId identificador de la categoría asociada al producto.
 * @param status estado actual del producto, representado por
 *               {@link ProductStatus}.
 * @param createdAt fecha y hora de creación del producto.
 * @param updatedAt fecha y hora de la última modificación del producto.
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
