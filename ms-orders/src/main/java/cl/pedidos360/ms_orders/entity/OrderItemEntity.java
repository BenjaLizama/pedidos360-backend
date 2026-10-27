package cl.pedidos360.ms_orders.entity;

import cl.pedidos360.ms_orders.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidad JPA que representa un ítem perteneciente a una orden de compra.
 *
 * <p>Almacena la información de cada producto incluido en una orden,
 * incluyendo su identificador, la cantidad solicitada y el precio unitario
 * registrado para dicho ítem.</p>
 *
 * <p>Extiende {@link AuditableEntity} para heredar el identificador único
 * y los campos de auditoría gestionados por Spring Data JPA.</p>
 *
 * <p>Mantiene una relación de muchos a uno con {@link OrderEntity},
 * permitiendo que varios ítems pertenezcan a una misma orden.</p>
 */
@Entity
@Table(name = "order_items")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class OrderItemEntity extends AuditableEntity {

    /**
     * Identificador único del producto asociado al ítem.
     *
     * <p>Corresponde al identificador del producto en el catálogo y se almacena
     * en la columna {@code product_id}. No establece una relación JPA directa
     * con una entidad de producto.</p>
     */
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    /**
     * Cantidad de unidades del producto incluidas en el ítem.
     *
     * <p>Este campo es obligatorio y se almacena en la columna {@code quantity}.</p>
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Precio unitario del producto registrado en el ítem de la orden.
     *
     * <p>Se almacena con una precisión de diez dígitos y dos posiciones
     * decimales. El valor es obligatorio.</p>
     */
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Orden a la que pertenece este ítem.
     *
     * <p>Representa el lado propietario de la relación bidireccional con
     * {@link OrderEntity}. La asociación se almacena mediante la columna
     * {@code order_id} y utiliza carga diferida para recuperar la orden
     * asociada cuando sea necesario.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;
}
