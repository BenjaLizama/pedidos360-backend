
package cl.pedidos360.bff.controller.docs;

import cl.pedidos360.bff.dto.response.PageResponse;
import cl.pedidos360.bff.dto.response.StandardResponse;
import cl.pedidos360.bff.dto.response.StockMovementResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

@Tag(
        name = "Movimientos de stock",
        description = """
                Operaciones de consulta del historial de movimientos
                de inventario mediante el BFF de Pedidos360.

                Los movimientos son generados por el microservicio
                de catálogo durante las operaciones de inventario.
                """
)
@SecurityRequirement(name = "bearerAuth")
public interface StockMovementControllerDocs {

    @Operation(
            summary = "Obtener movimientos de stock de un producto",
            description = """
                    Consulta el historial de movimientos de inventario
                    asociados a un producto específico.

                    Los parámetros de paginación y ordenamiento se reenvían
                    al microservicio de catálogo.

                    Requiere autenticación y uno de los siguientes roles:
                    ADMINISTRADOR u OPERADOR.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Movimientos de stock recuperados correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Identificador UUID o parámetros de consulta inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado; se requiere un token JWT válido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no posee los roles requeridos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado, si el catálogo devuelve este estado"
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Error de comunicación con el microservicio de catálogo"
            ),
            @ApiResponse(
                    responseCode = "504",
                    description = "Tiempo de espera agotado al consultar el catálogo"
            )
    })
    StandardResponse<PageResponse<StockMovementResponse>> getStockMovements(
            @PathVariable @Parameter(
                    name = "productId",
                    description = "Identificador único del producto",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID productId,

            @Parameter(
                    description = "Parámetros de paginación, ordenamiento y filtros admitidos por el catálogo"
            )
            @RequestParam
            Map<String, String> queryParams
    );
}
