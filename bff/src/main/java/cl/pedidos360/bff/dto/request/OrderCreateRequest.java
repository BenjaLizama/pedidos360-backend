package cl.pedidos360.bff.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO de solicitud utilizado para crear una nueva orden de compra.
 *
 * <p>Contiene la lista de productos que el cliente desea incluir en la orden.
 * Cada elemento de la lista se valida individualmente para asegurar que
 * cumpla con las restricciones definidas en {@link OrderItemRequest}.</p>
 *
 * <p>La solicitud no puede contener una lista vacía ni carecer de productos.</p>
 *
 * @param items lista de productos y cantidades que componen la orden.
 */
public record OrderCreateRequest(

        /**
         * Productos que se incluirán en la orden.
         *
         * <p>La lista debe contener al menos un elemento y cada solicitud
         * de producto se somete a validación.</p>
         */
        @NotEmpty(message = "La orden debe contener al menos un producto")
        List<@Valid OrderItemRequest> items

) {
}
