package cl.pedidos360.ms_catalog.entity;

import cl.pedidos360.ms_catalog.audit.AuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Documento que representa un cambio histórico de precio de un producto.
 *
 * <p>Los registros de esta colección son generados internamente por las
 * operaciones de modificación de precios y permiten conservar la
 * trazabilidad de los valores anteriores y nuevos.</p>
 *
 * <p>Extiende {@link AuditableEntity}, por lo que además registra quién
 * realizó el cambio y cuándo fue realizado.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "price_history")
@CompoundIndex(
        name = "product_created_at_idx",
        def = "{'productId': 1, 'createdAt': -1}"
)
public class PriceHistoryEntity extends AuditableEntity {

    /**
     * Identificador único del registro histórico.
     */
    @Id
    private UUID id = UUID.randomUUID();

    /**
     * Identificador del producto cuyo precio fue modificado.
     *
     * <p>Se almacena como UUID en lugar de utilizar una referencia
     * {@code @DBRef}, manteniendo un modelo desacoplado entre documentos.</p>
     */
    private UUID productId;

    /**
     * Precio que tenía el producto antes de la modificación.
     */
    private BigDecimal previousPrice;

    /**
     * Precio establecido después de la modificación.
     */
    private BigDecimal newPrice;

    /**
     * Motivo asociado al cambio de precio.
     *
     * <p>Permite proporcionar contexto funcional sobre la modificación.</p>
     */
    private String reason;

    /**
     * Identificador de correlación de la operación que originó
     * el cambio.
     *
     * <p>Permite relacionar este registro con logs, trazas distribuidas
     * y operaciones provenientes de otros componentes del sistema.</p>
     */
    private UUID correlationId;
}
