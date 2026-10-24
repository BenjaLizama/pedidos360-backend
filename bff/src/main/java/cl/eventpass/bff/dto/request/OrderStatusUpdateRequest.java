package cl.eventpass.bff.dto.request;

import cl.eventpass.bff.enums.OrderStateEnum;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud utilizado para actualizar el estado de una orden.
 *
 * <p>Permite indicar el nuevo estado que se desea asignar a una orden
 * mediante un valor del enumerado {@link OrderStateEnum}.</p>
 *
 * <p>El estado es obligatorio y debe corresponder a uno de los valores
 * definidos en dicho enumerado. Las reglas que determinan si la transición
 * de estado está permitida se validan en la capa de servicio.</p>
 *
 * @param status nuevo estado solicitado para la orden.
 */
public record OrderStatusUpdateRequest(

        /**
         * Nuevo estado que se desea establecer para la orden.
         */
        @NotNull(message = "El nuevo estado de la orden es obligatorio")
        OrderStateEnum status

) {
}
