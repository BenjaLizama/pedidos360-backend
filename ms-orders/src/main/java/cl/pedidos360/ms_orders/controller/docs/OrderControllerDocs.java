package cl.pedidos360.ms_orders.controller.docs;

import cl.pedidos360.ms_orders.dto.request.OrderCreateRequest;
import cl.pedidos360.ms_orders.dto.request.OrderStatusUpdateRequest;
import cl.pedidos360.ms_orders.dto.response.OrderResponse;
import cl.pedidos360.ms_orders.dto.response.StandardErrorResponse;
import cl.pedidos360.ms_orders.dto.response.StandardResponse;
import cl.pedidos360.ms_orders.enums.OrderStateEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

/**
 * Documentación OpenAPI de los endpoints para la gestión de órdenes.
 *
 * <p>Centraliza las descripciones de Swagger y mantiene el controlador
 * separado de los detalles de documentación.</p>
 */
@Tag(
        name = "Órdenes",
        description = "Endpoints para crear, consultar, cancelar y actualizar órdenes de compra."
)
public interface OrderControllerDocs {

    @Operation(
            summary = "Crear una orden",
            description = """
                    Registra una nueva orden para el cliente autenticado.
                    La identidad del cliente debe determinarse a partir del
                    contexto de seguridad y no confiarse a datos enviados
                    arbitrariamente en la solicitud.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Orden creada correctamente",
                    content = @Content(
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La solicitud contiene datos inválidos",
                    content = @Content(
                            schema = @Schema(implementation = StandardErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No existe una autenticación válida",
                    content = @Content(
                            schema = @Schema(implementation = StandardErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para crear órdenes",
                    content = @Content(
                            schema = @Schema(implementation = StandardErrorResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<OrderResponse>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos necesarios para crear la orden",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = OrderCreateRequest.class)
                    )
            )
            @ValidRequestBody OrderCreateRequest request
    );

    @Operation(
            summary = "Listar todas las órdenes",
            description = """
                    Consulta las órdenes registradas con filtros opcionales
                    por cliente y estado. Disponible para operadores y
                    administradores. Los resultados están paginados.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Órdenes consultadas correctamente",
                    content = @Content(
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No existe una autenticación válida",
                    content = @Content(
                            schema = @Schema(implementation = StandardErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Se requiere el rol OPERADOR o ADMIN",
                    content = @Content(
                            schema = @Schema(implementation = StandardErrorResponse.class)
                    )
            )
    })
    @Parameter(
            name = "customerId",
            description = "UUID del cliente por el que se desea filtrar",
            in = ParameterIn.QUERY
    )
    @Parameter(
            name = "status",
            description = "Estado por el que se desea filtrar",
            in = ParameterIn.QUERY
    )
    ResponseEntity<StandardResponse<Page<OrderResponse>>> findAll(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) OrderStateEnum status,
            @Parameter(description = "Parámetros de paginación y ordenamiento")
            Pageable pageable
    );

    @Operation(
            summary = "Consultar mis órdenes",
            description = """
                    Devuelve únicamente las órdenes asociadas al cliente
                    autenticado, con soporte para paginación.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Órdenes del cliente consultadas correctamente",
                    content = @Content(
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No existe una autenticación válida",
                    content = @Content(
                            schema = @Schema(implementation = StandardErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Se requiere el rol CLIENTE",
                    content = @Content(
                            schema = @Schema(implementation = StandardErrorResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<Page<OrderResponse>>> findMyOrders(
            @Parameter(description = "Parámetros de paginación y ordenamiento")
            Pageable pageable
    );

    @Operation(
            summary = "Consultar una orden por ID",
            description = """
                    Obtiene el detalle de una orden específica.
                    El servicio debe validar la propiedad de la orden cuando
                    el solicitante sea un cliente.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Orden encontrada",
                    content = @Content(
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No existe una autenticación válida",
                    content = @Content(
                            schema = @Schema(implementation = StandardErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permiso para consultar esta orden",
                    content = @Content(
                            schema = @Schema(implementation = StandardErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe una orden con el identificador indicado",
                    content = @Content(
                            schema = @Schema(implementation = StandardErrorResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<OrderResponse>> findById(
            @Parameter(
                    description = "Identificador único de la orden",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID orderId
    );

    @Operation(
            summary = "Cancelar una orden",
            description = """
                    Solicita la cancelación de una orden del cliente autenticado.
                    El servicio debe validar la propiedad y que el estado actual
                    permita la cancelación.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Orden cancelada correctamente",
                    content = @Content(
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No existe una autenticación válida",
                    content = @Content(
                            schema = @Schema(implementation = StandardErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permiso para cancelar esta orden",
                    content = @Content(
                            schema = @Schema(implementation = StandardErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe una orden con el identificador indicado",
                    content = @Content(
                            schema = @Schema(implementation = StandardErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El estado de la orden no permite cancelarla",
                    content = @Content(
                            schema = @Schema(implementation = StandardErrorResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<OrderResponse>> cancel(
            @Parameter(
                    description = "Identificador único de la orden que se desea cancelar",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID orderId
    );

    @Operation(
            summary = "Actualizar el estado de una orden",
            description = """
                    Actualiza el estado de una orden existente.
                    Disponible para operadores y administradores.
                    El servicio debe validar la transición de estado solicitada.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado de la orden actualizado correctamente",
                    content = @Content(
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La solicitud contiene un estado inválido",
                    content = @Content(
                            schema = @Schema(implementation = StandardErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No existe una autenticación válida",
                    content = @Content(
                            schema = @Schema(implementation = StandardErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Se requiere el rol OPERADOR o ADMIN",
                    content = @Content(
                            schema = @Schema(implementation = StandardErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe una orden con el identificador indicado",
                    content = @Content(
                            schema = @Schema(implementation = StandardErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La transición de estado no está permitida",
                    content = @Content(
                            schema = @Schema(implementation = StandardErrorResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<OrderResponse>> updateStatus(
            @Parameter(
                    description = "Identificador único de la orden que se desea actualizar",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID orderId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nuevo estado que se asignará a la orden",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = OrderStatusUpdateRequest.class)
                    )
            )
            @ValidRequestBody OrderStatusUpdateRequest request
    );

    /**
     * Anotación auxiliar para indicar que el cuerpo de la solicitud
     * debe validarse en la implementación del controlador.
     *
     * <p>La validación real se aplica con {@code @Valid} en el controlador.</p>
     */
    @interface ValidRequestBody {
    }
}
