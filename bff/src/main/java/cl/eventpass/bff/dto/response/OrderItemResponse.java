package cl.eventpass.bff.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de respuesta que representa un producto dentro de una orden de compra.
 *
 * <p>Expone la información del ítem, incluyendo el identificador del producto,
 * la cantidad solicitada, el precio unitario y el subtotal correspondiente.</p>
 *
 * <p>El subtotal representa el importe asociado a la cantidad de unidades
 * del producto incluidas en el ítem.</p>
 *
 * @param id identificador único del ítem de la orden.
 * @param productId identificador único del producto del catálogo.
 * @param quantity cantidad de unidades del producto solicitadas.
 * @param unitPrice precio unitario del producto registrado en el ítem.
 * @param subtotal importe correspondiente al precio unitario multiplicado
 *                 por la cantidad solicitada.
 */
public record OrderItemResponse(

        UUID id,
        UUID productId,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal

) {
}
