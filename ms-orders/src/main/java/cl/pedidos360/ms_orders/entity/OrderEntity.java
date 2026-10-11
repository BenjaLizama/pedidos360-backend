package cl.pedidos360.ms_orders.entity;

import cl.pedidos360.ms_orders.audit.AuditableEntity;
import cl.pedidos360.ms_orders.enums.OrderStateEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter @Setter
public class OrderEntity extends AuditableEntity {

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_state", nullable = false)
    private OrderStateEnum orderState;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItemEntity> items = new ArrayList<>();

    /**
     * Método utilitario para agregar ítems y mantener la sincronización
     * de la relación bidireccional automáticamente.
     */
    public void addItem(OrderItemEntity item) {
        items.add(item);
        item.setOrder(this);
    }
}
