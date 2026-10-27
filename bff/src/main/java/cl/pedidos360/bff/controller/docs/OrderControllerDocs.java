package cl.pedidos360.bff.controller.docs;

import java.util.UUID;

import cl.pedidos360.bff.dto.request.OrderCreateRequest;
import cl.pedidos360.bff.dto.request.OrderStatusUpdateRequest;
import cl.pedidos360.bff.dto.response.DashboardSummaryResponse;
import cl.pedidos360.bff.dto.response.OrderResponse;
import cl.pedidos360.bff.dto.response.StandardResponse;
import cl.pedidos360.bff.enums.OrderStateEnum;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(
        name = "Órdenes",
        description = """
                Operaciones para crear, consultar, cancelar y actualizar
                órdenes de compra mediante el BFF de Pedidos360.
                Las solicitudes se delegan al microservicio de órdenes.
                """
)
@SecurityRequirement(name = "bearerAuth")
public interface OrderControllerDocs {

    @Operation(
            summary = "Crear una orden",
            description = """
                    Registra una orden para el cliente autenticado.
                    La identidad del cliente debe obtenerse del contexto
                    de seguridad, no de un identificador arbitrario
                    proporcionado en la solicitud.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Orden creada correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No existe una autenticación válida"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para crear órdenes"
            )
    })
    StandardResponse<OrderResponse> createOrder(
            @RequestBody(
                    description = "Datos necesarios para crear la orden",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = OrderCreateRequest.class
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody
            OrderCreateRequest request
    );

    @Operation(
            summary = "Listar todas las órdenes",
            description = """
                    Consulta las órdenes con filtros opcionales por cliente
                    y estado, incluyendo paginación y ordenamiento.
                    Requiere los permisos definidos para consultar todas
                    las órdenes.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Órdenes consultadas correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Filtros o parámetros de paginación inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No existe una autenticación válida"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para consultar todas las órdenes"
            )
    })
    @io.swagger.v3.oas.annotations.Parameters({
            @Parameter(
                    name = "customerId",
                    description = "UUID del cliente por el que se desea filtrar",
                    in = ParameterIn.QUERY
            ),
            @Parameter(
                    name = "status",
                    description = "Estado por el que se desea filtrar",
                    in = ParameterIn.QUERY
            )
    })
    StandardResponse<Page<OrderResponse>> findAll(
            @RequestParam(required = false)
            UUID customerId,

            @RequestParam(required = false)
            OrderStateEnum status,

            @ParameterObject
            Pageable pageable
    );

    @Operation(
            summary = "Consultar mis órdenes",
            description = """
                    Devuelve únicamente las órdenes asociadas al cliente
                    autenticado, con soporte para paginación y ordenamiento.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Órdenes del cliente consultadas correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No existe una autenticación válida"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para consultar sus órdenes"
            )
    })
    StandardResponse<Page<OrderResponse>> findMyOrders(
            @ParameterObject
            Pageable pageable
    );

    @Operation(
            summary = "Consultar una orden por ID",
            description = """
                    Obtiene el detalle de una orden.
                    El microservicio de órdenes valida los permisos
                    y la propiedad de la orden cuando corresponda.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Orden encontrada",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Identificador UUID inválido"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No existe una autenticación válida"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no puede consultar esta orden"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La orden no existe"
            )
    })
    StandardResponse<OrderResponse> findById(
            @PathVariable @Parameter(
                    name = "orderId",
                    description = "Identificador único de la orden",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID orderId
    );

    @Operation(
            summary = "Cancelar una orden",
            description = """
                    Solicita cancelar una orden.
                    El microservicio valida los permisos del usuario
                    y si el estado actual permite la cancelación.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Orden cancelada correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No existe una autenticación válida"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no puede cancelar esta orden"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La orden no existe"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El estado de la orden no permite cancelarla"
            )
    })
    StandardResponse<OrderResponse> cancel(
            @PathVariable @Parameter(
                    name = "orderId",
                    description = "Identificador único de la orden",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID orderId
    );

    @Operation(
            summary = "Actualizar el estado de una orden",
            description = """
                    Solicita actualizar el estado de una orden existente.
                    El microservicio valida los permisos y la transición
                    de estado solicitada.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado actualizado correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Estado inválido"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No existe una autenticación válida"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para actualizar el estado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La orden no existe"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La transición de estado no está permitida"
            )
    })
    StandardResponse<OrderResponse> updateStatus(
            @PathVariable @Parameter(
                    name = "orderId",
                    description = "Identificador único de la orden",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID orderId,

            @RequestBody(
                    description = "Estado que se desea asignar a la orden",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = OrderStatusUpdateRequest.class
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody
            OrderStatusUpdateRequest request
    );

    @Operation(
            summary = "Aceptar una orden",
            description = """
                    Solicita aceptar una orden.
                    El microservicio valida el estado actual y coordina
                    la reducción del stock con el catálogo antes de
                    confirmar la aceptación.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Orden aceptada correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No existe una autenticación válida"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para aceptar órdenes"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La orden no existe"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La orden no puede aceptarse o no hay stock suficiente"
            )
    })
    StandardResponse<OrderResponse> accept(
            @PathVariable @Parameter(
                    name = "orderId",
                    description = "Identificador único de la orden",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID orderId
    );

    @Operation(
            summary = "Obtener resumen del dashboard",
            description = """
                    Obtiene el resumen de órdenes, incluyendo cantidades
                    agrupadas por estado y órdenes pendientes de aceptación.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Resumen obtenido correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No existe una autenticación válida"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para consultar el dashboard"
            )
    })
    StandardResponse<DashboardSummaryResponse> getDashboardSummary();
}
