package cl.pedidos360.ms_orders.entity;

import cl.pedidos360.ms_orders.audit.AuditableEntity;
import cl.pedidos360.ms_orders.enums.OrderStateEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA que representa una orden de compra en el sistema Pedidos360.
 *
 * <p>Almacena la información principal de la orden, incluyendo el identificador
 * del cliente, el estado actual, el importe total y la lista de ítems asociados.</p>
 *
 * <p>Extiende {@link AuditableEntity} para heredar el identificador único
 * y los campos de auditoría gestionados por Spring Data JPA.</p>
 *
 * <p>Mantiene una relación bidireccional de uno a muchos con
 * {@link OrderItemEntity}, permitiendo asociar múltiples ítems a una misma
 * orden y gestionar su persistencia mediante operaciones en cascada.</p>
 */
@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class OrderEntity extends AuditableEntity {

    /**
     * Identificador único del cliente propietario de la orden.
     *
     * <p>Este campo es obligatorio y se almacena en la columna {@code client_id}.</p>
     */
    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    /**
     * Estado actual de la orden de compra.
     *
     * <p>El estado se almacena como una cadena de texto utilizando los valores
     * definidos en {@link OrderStateEnum}.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "order_state", nullable = false)
    private OrderStateEnum orderState;

    /**
     * Importe total de la orden de compra.
     *
     * <p>Se almacena con una precisión de diez dígitos y dos posiciones
     * decimales. El valor es obligatorio.</p>
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    /**
     * Lista de ítems asociados a la orden.
     *
     * <p>La relación se administra desde el atributo {@code order} de
     * {@link OrderItemEntity}. Las operaciones de persistencia se propagan
     * a los ítems mediante cascada y los ítems eliminados de la colección
     * se eliminan de la base de datos debido a {@code orphanRemoval}.</p>
     *
     * <p>La lista se inicializa vacía por defecto al utilizar el patrón
     * Builder de Lombok.</p>
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItemEntity> items = new ArrayList<>();

    /**
     * Agrega un ítem a la orden y sincroniza ambos lados de la relación.
     *
     * <p>Incorpora el ítem a la colección de la orden y establece esta entidad
     * como la orden asociada al ítem.</p>
     *
     * @param item ítem que se desea agregar a la orden.
     */
    public void addItem(OrderItemEntity item) {
        items.add(item);
        item.setOrder(this);
    }
}
