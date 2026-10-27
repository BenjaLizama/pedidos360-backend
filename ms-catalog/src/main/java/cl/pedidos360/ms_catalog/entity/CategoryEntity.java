package cl.pedidos360.ms_catalog.entity;

import cl.pedidos360.ms_catalog.audit.AuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

/**
 * Documento que representa una categoría dentro del catálogo.
 *
 * <p>Las categorías son administradas como entidades dinámicas,
 * permitiendo su creación, modificación y desactivación sin necesidad
 * de modificar el código de la aplicación.</p>
 *
 * <p>Extiende {@link AuditableEntity} para registrar automáticamente
 * información sobre su creación y última modificación.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "categories")
public class CategoryEntity extends AuditableEntity {

    /**
     * Identificador único de la categoría.
     */
    @Id
    private UUID id = UUID.randomUUID();

    /**
     * Nombre único de la categoría.
     *
     * <p>El índice utiliza una collation con nivel de comparación
     * secundario para garantizar unicidad sin distinguir entre
     * mayúsculas y minúsculas.</p>
     *
     * <p>Por ejemplo, los valores {@code Bebidas}, {@code bebidas}
     * y {@code BEBIDAS} se consideran equivalentes.</p>
     */
    @Indexed(
            unique = true,
            collation = "{ 'locale': 'es', 'strength': 2 }"
    )
    private String name;

    /**
     * Descripción opcional de la categoría.
     */
    private String description;

    /**
     * Indica si la categoría se encuentra disponible para su utilización.
     *
     * <p>La desactivación permite conservar la categoría y su historial
     * sin eliminar físicamente el documento de MongoDB.</p>
     */
    private boolean active = true;

    /**
     * Versión utilizada para control de concurrencia optimista.
     *
     * <p>Permite detectar modificaciones concurrentes sobre el mismo
     * documento y evitar sobrescrituras accidentales.</p>
     */
    @Version
    private Long version;
}
