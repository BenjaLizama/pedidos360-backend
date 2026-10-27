package cl.pedidos360.ms_catalog.controller.docs;

import cl.pedidos360.ms_catalog.dto.request.*;
import cl.pedidos360.ms_catalog.dto.response.ProductResponse;
import cl.pedidos360.ms_catalog.dto.response.StandardResponse;
import cl.pedidos360.ms_catalog.enums.ProductStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Productos",
        description = "Operaciones para crear, consultar y administrar productos del catálogo, "
                + "incluyendo precios, stock y estado."
)
public interface ProductControllerDocs {

    @Operation(
            summary = "Crear un producto",
            description = "Registra un nuevo producto en el catálogo. "
                    + "El SKU debe ser único. Si el producto se crea con stock inicial mayor a cero, "
                    + "se registra automáticamente un movimiento de carga inicial. "
                    + "Requiere el rol ADMINISTRADOR u OPERADOR.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Producto creado correctamente.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Los datos enviados no superan las validaciones.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No se proporcionó un token válido.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para crear productos.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Ya existe un producto con el SKU indicado.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            )
    })
    ResponseEntity<StandardResponse<ProductResponse>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del producto que se desea crear.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProductCreateRequest.class)
                    )
            )
            @Valid ProductCreateRequest request
    );

    @Operation(
            summary = "Obtener un producto por su ID",
            description = "Recupera la información de un producto mediante su identificador único. "
                    + "Este endpoint no requiere autenticación."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto recuperado correctamente.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El identificador proporcionado no tiene un formato válido.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe un producto con el identificador indicado.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            )
    })
    ResponseEntity<StandardResponse<ProductResponse>> findById(
            @Parameter(
                    name = "productId",
                    description = "Identificador UUID del producto.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID productId
    );

    @Operation(
            summary = "Buscar productos",
            description = "Obtiene una página de productos aplicando filtros opcionales por nombre, "
                    + "categoría y estado. Permite controlar la paginación y el orden de los resultados. "
                    + "No requiere autenticación."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Productos recuperados correctamente.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Algún filtro, estado o parámetro de paginación no es válido.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            )
    })
    ResponseEntity<StandardResponse<Page<ProductResponse>>> search(
            @Parameter(
                    name = "name",
                    description = "Nombre completo o parcial del producto.",
                    example = "Teclado mecánico"
            )
            String name,

            @Parameter(
                    name = "categoryId",
                    description = "Identificador UUID de la categoría.",
                    example = "550e8400-e29b-41d4-a716-446655440001"
            )
            UUID categoryId,

            @Parameter(
                    name = "status",
                    description = "Estado por el cual filtrar los productos.",
                    schema = @Schema(implementation = ProductStatus.class),
                    example = "ACTIVE"
            )
            ProductStatus status,

            @ParameterObject Pageable pageable
    );

    @Operation(
            summary = "Actualizar los datos de un producto",
            description = "Actualiza los campos generales permitidos por ProductUpdateRequest. "
                    + "El precio y el stock se administran mediante sus endpoints específicos. "
                    + "Requiere el rol ADMINISTRADOR u OPERADOR.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto actualizado correctamente.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Los datos enviados no superan las validaciones.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No se proporcionó un token válido.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para actualizar productos.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe un producto con el identificador indicado.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            )
    })
    ResponseEntity<StandardResponse<ProductResponse>> update(
            @Parameter(
                    name = "productId",
                    description = "Identificador UUID del producto que se actualizará.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID productId,

            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nuevos datos generales del producto.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProductUpdateRequest.class)
                    )
            )
            ProductUpdateRequest request
    );

    @Operation(
            summary = "Actualizar el estado de un producto",
            description = "Modifica el estado del producto según los valores permitidos por ProductStatus. "
                    + "Requiere el rol ADMINISTRADOR u OPERADOR.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado del producto actualizado correctamente.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El estado enviado no es válido.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No se proporcionó un token válido.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para cambiar el estado.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe un producto con el identificador indicado.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            )
    })
    ResponseEntity<StandardResponse<ProductResponse>> updateStatus(
            @Parameter(
                    name = "productId",
                    description = "Identificador UUID del producto.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID productId,

            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Estado que se asignará al producto.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProductStatusUpdateRequest.class)
                    )
            )
            ProductStatusUpdateRequest request
    );

    @Operation(
            summary = "Actualizar el precio de un producto",
            description = "Cambia el precio del producto y registra el cambio en el historial de precios, "
                    + "incluyendo el precio anterior, el nuevo precio y el motivo informado. "
                    + "No permite establecer el mismo precio actual. "
                    + "Requiere el rol ADMINISTRADOR u OPERADOR.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Precio actualizado y cambio registrado en el historial.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El precio o el motivo no cumplen las validaciones.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No se proporcionó un token válido.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para modificar precios.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe un producto con el identificador indicado.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El precio nuevo es igual al precio actual, si esta excepción se mapea a HTTP 409.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            )
    })
    ResponseEntity<StandardResponse<ProductResponse>> updatePrice(
            @Parameter(
                    name = "productId",
                    description = "Identificador UUID del producto.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID productId,

            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nuevo precio y motivo del cambio.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProductPriceUpdateRequest.class)
                    )
            )
            ProductPriceUpdateRequest request
    );

    @Operation(
            summary = "Ajustar el stock de un producto",
            description = "Incrementa o disminuye el stock según la operación solicitada. "
                    + "No permite disminuir una cantidad superior al stock disponible. "
                    + "Cada ajuste genera un movimiento de tipo ADJUSTMENT. "
                    + "Requiere el rol ADMINISTRADOR u OPERADOR.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock ajustado y movimiento registrado correctamente.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La operación, cantidad o motivo no cumplen las validaciones.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No se proporcionó un token válido.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para ajustar el stock.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe un producto con el identificador indicado.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "No existe stock suficiente para realizar la disminución, si la excepción se mapea a HTTP 409.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            )
    })
    ResponseEntity<StandardResponse<ProductResponse>> updateStock(
            @Parameter(
                    name = "productId",
                    description = "Identificador UUID del producto.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID productId,

            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Operación, cantidad y motivo del ajuste de stock.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProductStockUpdateRequest.class)
                    )
            )
            ProductStockUpdateRequest request
    );

    @Operation(
            summary = "Reponer stock de un producto",
            description = "Incrementa el stock actual con la cantidad solicitada y registra "
                    + "un movimiento de tipo RESTOCK. Requiere el rol ADMINISTRADOR u OPERADOR.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock repuesto y movimiento registrado correctamente.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La cantidad o el motivo no cumplen las validaciones.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No se proporcionó un token válido.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para reponer stock.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe un producto con el identificador indicado.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            )
    })
    ResponseEntity<StandardResponse<ProductResponse>> restock(
            @Parameter(
                    name = "productId",
                    description = "Identificador UUID del producto.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID productId,

            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Cantidad que se agregará y motivo de la reposición.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProductRestockRequest.class)
                    )
            )
            ProductRestockRequest request
    );

    @Operation(
            summary = "Eliminar un producto",
            description = "Realiza una eliminación lógica cambiando el estado del producto a INACTIVE. "
                    + "El registro se conserva para mantener la trazabilidad histórica. "
                    + "Esta operación requiere el rol ADMINISTRADOR.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto eliminado lógicamente.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No se proporcionó un token válido.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene el rol ADMINISTRADOR.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe un producto con el identificador indicado.",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class))
            )
    })
    ResponseEntity<StandardResponse<Void>> delete(
            @Parameter(
                    name = "productId",
                    description = "Identificador UUID del producto que se eliminará.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID productId
    );

    @Operation(
            summary = "Reducir stock de múltiples productos",
            description = "Reduce el stock de una lista de productos de forma individual."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock actualizado correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solicitud inválida o lista vacía"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Uno o más productos no fueron encontrados"
            )
    })
    ResponseEntity<StandardResponse<Void>> bulkReduceStock(
            @Valid
            @RequestBody
            @NotEmpty(message = "Debe indicar al menos un producto.")
            List<@Valid ProductStockReductionItemRequest> request
    );
}
