package cl.pedidos360.ms_catalog.dto.request;

import cl.pedidos360.ms_catalog.enums.StockAdjustmentOperation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos necesarios para realizar un ajuste manual de inventario.
 *
 * <p>La operación utiliza una cantidad positiva y especifica
 * explícitamente si el ajuste debe aumentar o disminuir el stock.</p>
 *
 * @param quantity cantidad de unidades involucradas en el ajuste
 * @param operation operación que determina si el stock aumenta o disminuye
 * @param reason motivo del ajuste manual de inventario
 */
public record ProductStockUpdateRequest(

        @NotNull(message = "La cantidad es obligatoria.")
        @Positive(message = "La cantidad debe ser mayor a cero.")
        Integer quantity,

        @NotNull(message = "La operación es obligatoria.")
        StockAdjustmentOperation operation,

        @NotBlank(message = "El motivo es obligatorio.")
        @Size(
                max = 300,
                message = "El motivo no puede superar los 300 caracteres."
        )
        String reason
) {
}
