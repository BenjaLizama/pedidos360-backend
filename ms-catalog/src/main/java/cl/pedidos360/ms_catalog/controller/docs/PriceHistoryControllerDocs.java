package cl.pedidos360.ms_catalog.controller.docs;

import cl.pedidos360.ms_catalog.dto.response.PriceHistoryResponse;
import cl.pedidos360.ms_catalog.dto.response.StandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(
        name = "Historial de precios",
        description = """
                Operaciones de consulta relacionadas con el historial
                de precios de los productos del catálogo.
                
                Los registros de historial son generados automáticamente
                cuando se modifica el precio de un producto.
                """
)
public interface PriceHistoryControllerDocs {

    @Operation(
            summary = "Obtener historial de precios de un producto",
            description = """
                    Obtiene el historial de modificaciones de precio
                    correspondiente a un producto específico.
                    
                    La información se entrega de forma paginada y permite
                    aplicar criterios de ordenamiento mediante los
                    parámetros de Pageable.
                    
                    Los registros no pueden ser creados, modificados
                    ni eliminados mediante este recurso.
                    
                    Requiere autenticación y uno de los siguientes roles:
                    ADMIN u OPERADOR.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Historial de precios recuperado correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            Solicitud inválida. Puede ocurrir cuando el
                            identificador del producto no posee un formato
                            UUID válido o los parámetros de paginación
                            son incorrectos.
                            """,
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere un token JWT válido.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = """
                            Acceso denegado. El usuario autenticado no posee
                            el rol ADMIN u OPERADOR requerido.
                            """,
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            )
    })
    ResponseEntity<StandardResponse<Page<PriceHistoryResponse>>> findByProductId(

            @Parameter(
                    name = "productId",
                    description = "Identificador único del producto",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID productId,

            @ParameterObject
            Pageable pageable
    );
}
