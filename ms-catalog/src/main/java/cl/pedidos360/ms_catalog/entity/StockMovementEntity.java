package cl.pedidos360.ms_catalog.entity;

import cl.pedidos360.ms_catalog.audit.AuditableEntity;
import cl.pedidos360.ms_catalog.enums.StockMovementType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

/**
 * Documento que representa un movimiento de inventario de un producto.
 *
 * <p>Cada modificación relevante del stock genera un registro histórico
 * que permite conocer cómo evolucionó el inventario y cuál fue el motivo
 * de cada operación.</p>
 *
 * <p>Extiende {@link AuditableEntity} para registrar automáticamente
 * quién realizó la operación y cuándo ocurrió.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "stock_movements")
@CompoundIndex(
        name = "product_created_at_idx",
        def = "{'productId': 1, 'createdAt': -1}"
)
public class StockMovementEntity extends AuditableEntity {

    /**
     * Identificador único del movimiento.
     */
    @Id
    private UUID id = UUID.randomUUID();

    /**
     * Identificador del producto afectado por el movimiento.
     */
    private UUID productId;

    /**
     * Tipo de operación que originó el movimiento de inventario.
     */
    private StockMovementType type;

    /**
     * Cantidad involucrada en el movimiento.
     *
     * <p>La cantidad representa siempre una magnitud positiva.
     * El tipo de movimiento determina el significado de la operación.</p>
     */
    private Integer quantity;

    /**
     * Stock disponible antes de ejecutar la operación.
     */
    private Integer previousStock;

    /**
     * Stock resultante después de ejecutar la operación.
     */
    private Integer resultingStock;

    /**
     * Motivo asociado al movimiento de inventario.
     */
    private String reason;

    /**
     * Identificador de correlación de la operación.
     *
     * <p>Permite relacionar el movimiento con logs, trazas distribuidas
     * y mensajes procesados desde otros microservicios.</p>
     */
    private UUID correlationId;

    /**
     * Identificador del usuario que realizó el movimiento de stock.
     *
     * <p>Proporciona auditoría sobre quién fue responsable de la
     * operación de inventario.</p>
     */
    private UUID executedBy;
}
