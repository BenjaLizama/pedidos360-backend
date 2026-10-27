package cl.pedidos360.ms_catalog.entity;

import cl.pedidos360.ms_catalog.audit.AuditableEntity;
import cl.pedidos360.ms_catalog.enums.ProductStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Documento principal del catálogo que representa un producto.
 *
 * <p>Contiene la información comercial y de inventario necesaria para
 * gestionar un producto dentro de Pedidos360.</p>
 *
 * <p>Extiende {@link AuditableEntity} para mantener trazabilidad sobre
 * la creación y modificación del documento.</p>
 *
 * <p>La entidad utiliza UUID como identificador y almacena la categoría
 * mediante su identificador, evitando acoplar directamente documentos
 * de MongoDB mediante {@code @DBRef}.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "products")
@CompoundIndex(
        name = "category_status_idx",
        def = "{'categoryId': 1, 'status': 1}"
)
public class ProductEntity extends AuditableEntity {

    /**
     * Identificador único del producto.
     */
    @Id
    private UUID id = UUID.randomUUID();

    /**
     * Identificador comercial único del producto.
     *
     * <p>El índice único garantiza que no existan dos productos
     * registrados con el mismo SKU.</p>
     */
    @Indexed(unique = true)
    private String sku;

    /**
     * Nombre comercial del producto.
     */
    private String name;

    /**
     * Descripción del producto.
     */
    private String description;

    /**
     * Precio actual del producto.
     *
     * <p>Los cambios de precio deben realizarse mediante la operación
     * específica de actualización de precio para permitir registrar
     * correctamente su historial.</p>
     */
    private BigDecimal price;

    /**
     * Cantidad actual disponible en inventario.
     *
     * <p>El valor no puede ser negativo y debe modificarse mediante
     * las operaciones de inventario definidas por el dominio.</p>
     */
    private Integer stock;

    /**
     * Identificador de la categoría a la que pertenece el producto.
     *
     * <p>Se almacena únicamente el UUID de la categoría para mantener
     * los documentos desacoplados.</p>
     */
    private UUID categoryId;

    /**
     * Estado actual del producto dentro del catálogo.
     */
    @Indexed
    private ProductStatus status = ProductStatus.ACTIVE;

    /**
     * Versión utilizada para control de concurrencia optimista.
     *
     * <p>Permite detectar modificaciones concurrentes y evitar que una
     * operación sobrescriba cambios realizados simultáneamente por
     * otro proceso.</p>
     */
    @Version
    private Long version;
}
