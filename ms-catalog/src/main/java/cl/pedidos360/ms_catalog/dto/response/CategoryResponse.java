package cl.pedidos360.ms_catalog.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Respuesta utilizada para representar una categoría del catálogo.
 *
 * <p>Expone la información necesaria para consultar la categoría,
 * incluyendo su estado y los datos básicos de auditoría.</p>
 *
 * @param id identificador único de la categoría
 * @param name nombre de la categoría
 * @param description descripción de la categoría
 * @param active indica si la categoría se encuentra disponible
 * @param createdAt fecha y hora de creación
 * @param updatedAt fecha y hora de la última modificación
 */
public record CategoryResponse(
        UUID id,
        String name,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
