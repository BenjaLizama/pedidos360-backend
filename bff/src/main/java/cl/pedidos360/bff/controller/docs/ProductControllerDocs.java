package cl.pedidos360.bff.controller.docs;

import cl.pedidos360.bff.dto.request.BulkStockReductionItemRequest;
import cl.pedidos360.bff.dto.request.ProductCreateRequest;
import cl.pedidos360.bff.dto.request.ProductPriceUpdateRequest;
import cl.pedidos360.bff.dto.request.ProductRestockRequest;
import cl.pedidos360.bff.dto.request.ProductStatusUpdateRequest;
import cl.pedidos360.bff.dto.request.ProductStockUpdateRequest;
import cl.pedidos360.bff.dto.request.ProductUpdateRequest;
import cl.pedidos360.bff.dto.response.PageResponse;
import cl.pedidos360.bff.dto.response.ProductResponse;
import cl.pedidos360.bff.dto.response.StandardResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(
        name = "Productos",
        description = """
                Operaciones para consultar y administrar productos mediante
                el Backend for Frontend (BFF) de Pedidos360.

                Las solicitudes se delegan al microservicio de catálogo.
                El BFF no administra directamente los productos ni su stock.
                """
)
@SecurityRequirement(name = "bearerAuth")
public interface ProductControllerDocs {

    @Operation(
            summary = "Crear un producto",
            description = """
                    Registra un producto a través del microservicio de catálogo.
                    Requiere el rol ADMINISTRADOR u OPERADOR.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Producto creado correctamente",
                    content = @Content(
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para crear productos"),
            @ApiResponse(responseCode = "409", description = "El SKU ya existe"),
            @ApiResponse(responseCode = "502", description = "Error de comunicación con el catálogo"),
            @ApiResponse(responseCode = "504", description = "Tiempo de espera agotado al consultar el catálogo")
    })
    StandardResponse<ProductResponse> createProduct(
            @RequestBody(
                    description = "Datos requeridos para crear el producto.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProductCreateRequest.class)
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody
            ProductCreateRequest request
    );

    @Operation(
            summary = "Obtener un producto por ID",
            description = "Consulta un producto mediante su identificador UUID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto recuperado correctamente",
                    content = @Content(
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "El identificador UUID no es válido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "502", description = "Error de comunicación con el catálogo"),
            @ApiResponse(responseCode = "504", description = "Tiempo de espera agotado al consultar el catálogo")
    })
    StandardResponse<ProductResponse> getProductById(
            @PathVariable @Parameter(
                    name = "productId",
                    description = "Identificador único del producto",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID productId
    );

    @Operation(
            summary = "Buscar productos",
            description = """
                    Busca productos utilizando los filtros y parámetros
                    de paginación admitidos por el microservicio de catálogo.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Productos recuperados correctamente",
                    content = @Content(
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Filtros o parámetros de paginación inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "502", description = "Error de comunicación con el catálogo"),
            @ApiResponse(responseCode = "504", description = "Tiempo de espera agotado al consultar el catálogo")
    })
    StandardResponse<PageResponse<ProductResponse>> search(
            @Parameter(
                    description = "Filtros de búsqueda y parámetros de paginación admitidos por el catálogo."
            )
            @RequestParam
            Map<String, String> queryParams
    );

    @Operation(
            summary = "Actualizar los datos de un producto",
            description = """
                    Actualiza los datos generales de un producto.
                    El precio y el stock se gestionan mediante operaciones
                    específicas. Requiere ADMINISTRADOR u OPERADOR.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto actualizado correctamente",
                    content = @Content(
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para actualizar productos"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "502", description = "Error de comunicación con el catálogo"),
            @ApiResponse(responseCode = "504", description = "Tiempo de espera agotado al consultar el catálogo")
    })
    StandardResponse<ProductResponse> updateProduct(
            @PathVariable @Parameter(
                    name = "productId",
                    description = "Identificador único del producto",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID productId,

            @RequestBody(
                    description = "Campos generales que admite la actualización.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProductUpdateRequest.class)
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody
            ProductUpdateRequest request
    );

    @Operation(
            summary = "Actualizar el estado de un producto",
            description = "Modifica el estado del producto. Requiere ADMINISTRADOR u OPERADOR."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado actualizado correctamente",
                    content = @Content(
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para modificar el estado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "502", description = "Error de comunicación con el catálogo"),
            @ApiResponse(responseCode = "504", description = "Tiempo de espera agotado al consultar el catálogo")
    })
    StandardResponse<ProductResponse> updateProductStatus(
            @PathVariable @Parameter(
                    name = "productId",
                    description = "Identificador único del producto",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID productId,

            @RequestBody(
                    description = "Nuevo estado del producto.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProductStatusUpdateRequest.class)
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody
            ProductStatusUpdateRequest request
    );

    @Operation(
            summary = "Actualizar el precio de un producto",
            description = """
                    Actualiza el precio del producto y permite al catálogo
                    registrar el historial correspondiente.
                    Requiere ADMINISTRADOR u OPERADOR.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Precio actualizado correctamente",
                    content = @Content(
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Precio o motivo inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para modificar precios"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "409", description = "El cambio entra en conflicto con las reglas del catálogo"),
            @ApiResponse(responseCode = "502", description = "Error de comunicación con el catálogo"),
            @ApiResponse(responseCode = "504", description = "Tiempo de espera agotado al consultar el catálogo")
    })
    StandardResponse<ProductResponse> updateProductPrice(
            @PathVariable @Parameter(
                    name = "productId",
                    description = "Identificador único del producto",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID productId,

            @RequestBody(
                    description = "Nuevo precio y datos adicionales admitidos por el catálogo.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProductPriceUpdateRequest.class)
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody
            ProductPriceUpdateRequest request
    );

    @Operation(
            summary = "Ajustar el stock de un producto",
            description = """
                    Ajusta el stock y permite al catálogo registrar
                    el movimiento de inventario correspondiente.
                    Requiere ADMINISTRADOR u OPERADOR.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock actualizado correctamente",
                    content = @Content(
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Operación, cantidad o motivo inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para ajustar el stock"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "409", description = "No se puede realizar el ajuste solicitado"),
            @ApiResponse(responseCode = "502", description = "Error de comunicación con el catálogo"),
            @ApiResponse(responseCode = "504", description = "Tiempo de espera agotado al consultar el catálogo")
    })
    StandardResponse<ProductResponse> updateProductStock(
            @PathVariable @Parameter(
                    name = "productId",
                    description = "Identificador único del producto",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID productId,

            @RequestBody(
                    description = "Operación, cantidad y motivo del ajuste.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProductStockUpdateRequest.class)
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody
            ProductStockUpdateRequest request
    );

    @Operation(
            summary = "Reponer stock de un producto",
            description = """
                    Incrementa el stock y registra el movimiento correspondiente.
                    Requiere ADMINISTRADOR u OPERADOR.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock repuesto correctamente",
                    content = @Content(
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Cantidad o motivo inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para reponer stock"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "502", description = "Error de comunicación con el catálogo"),
            @ApiResponse(responseCode = "504", description = "Tiempo de espera agotado al consultar el catálogo")
    })
    StandardResponse<ProductResponse> restockProduct(
            @PathVariable @Parameter(
                    name = "productId",
                    description = "Identificador único del producto",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID productId,

            @RequestBody(
                    description = "Cantidad que se agregará y motivo de la reposición.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProductRestockRequest.class)
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody
            ProductRestockRequest request
    );

    @Operation(
            summary = "Eliminar un producto",
            description = "Solicita la eliminación lógica del producto. Requiere ADMINISTRADOR."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Producto eliminado sin contenido de respuesta",
                    content = @Content
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para eliminar productos"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "502", description = "Error de comunicación con el catálogo"),
            @ApiResponse(responseCode = "504", description = "Tiempo de espera agotado al consultar el catálogo")
    })
    void deleteProduct(
            @PathVariable @Parameter(
                    name = "productId",
                    description = "Identificador único del producto",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID productId
    );

    @Operation(
            summary = "Reducir stock de múltiples productos",
            description = """
                    Envía una lista de productos y cantidades para reducir
                    el stock de manera masiva.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Solicitud de reducción de stock procesada",
                    content = @Content(
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "La solicitud o la lista de productos es inválida"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para reducir stock"),
            @ApiResponse(responseCode = "404", description = "Uno o más productos no fueron encontrados"),
            @ApiResponse(responseCode = "502", description = "Error de comunicación con el catálogo"),
            @ApiResponse(responseCode = "504", description = "Tiempo de espera agotado al consultar el catálogo")
    })
    StandardResponse<?> bulkReduceStock(
            @RequestBody(
                    description = "Lista de productos y cantidades que se desean descontar.",
                    required = true,
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation = BulkStockReductionItemRequest.class
                                    )
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody
            List<BulkStockReductionItemRequest> request
    );
}
