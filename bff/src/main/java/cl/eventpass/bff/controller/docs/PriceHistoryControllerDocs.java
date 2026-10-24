package cl.eventpass.bff.controller.docs;

import cl.eventpass.bff.dto.response.PriceHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

@Tag(
        name = "Historial de precios",
        description = """
                Operaciones de consulta del historial de precios
                de los productos mediante el BFF de Pedidos360.

                Los registros son generados por el microservicio
                de catálogo cuando se producen modificaciones de precio.
                """
)
public interface PriceHistoryControllerDocs {

    @Operation(
            summary = "Obtener historial de precios de un producto",
            description = """
                    Consulta el historial de modificaciones de precio
                    de un producto mediante el microservicio de catálogo.

                    Los parámetros de consulta y paginación se reenvían
                    al servicio correspondiente.

                    Esta operación requiere autenticación y uno de los
                    roles autorizados por el microservicio:
                    ADMINISTRADOR u OPERADOR.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Historial de precios recuperado correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = PriceHistoryResponse.class
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
                    description = "El usuario no posee los permisos requeridos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró el producto o su historial"
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
    Page<PriceHistoryResponse> findByProductId(

            @PathVariable @Parameter(
                    name = "productId",
                    description = "Identificador único del producto",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            ) UUID productId,

            @Parameter(
                    description = """
                            Parámetros de consulta admitidos por el catálogo,
                            incluyendo paginación y ordenamiento.
                            """
            )
            @RequestParam Map<String, String> queryParams
    );
}
