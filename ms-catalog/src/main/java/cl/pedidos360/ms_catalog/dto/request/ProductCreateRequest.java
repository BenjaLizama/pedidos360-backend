package cl.pedidos360.ms_catalog.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Datos necesarios para registrar un nuevo producto en el catálogo.
 *
 * <p>La creación establece la información comercial inicial y el stock
 * inicial del producto. Las operaciones posteriores de precio, stock
 * y estado utilizan requests específicos.</p>
 *
 * @param sku código único utilizado para identificar el producto
 * @param name nombre del producto
 * @param description descripción opcional del producto
 * @param price precio inicial del producto
 * @param stock stock inicial disponible
 * @param categoryId identificador de la categoría asociada al producto.
 *                   Si no se especifica, se asignará la categoría OTROS.
 */
public record ProductCreateRequest(

        @NotBlank(message = "El SKU es obligatorio.")
        @Size(max = 50, message = "El SKU no puede superar los 50 caracteres.")
        @Pattern(
                regexp = "^[a-zA-Z0-9_-]+$",
                message = "El SKU solo puede contener letras, números, guiones y guiones bajos."
        )
        String sku,

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres.")
        String name,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres.")
        String description,

        @NotNull(message = "El precio es obligatorio.")
        @DecimalMin(
                value = "0.0",
                inclusive = false,
                message = "El precio debe ser mayor a cero."
        )
        BigDecimal price,

        @NotNull(message = "El stock es obligatorio.")
        @PositiveOrZero(message = "El stock no puede ser negativo.")
        Integer stock,

        UUID categoryId
) {
}
