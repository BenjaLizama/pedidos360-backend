package cl.pedidos360.ms_catalog.controller.docs;

import cl.pedidos360.ms_catalog.dto.response.StandardResponse;
import cl.pedidos360.ms_catalog.dto.response.StockMovementResponse;
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
        name = "Movimientos de stock",
        description = """
                Operaciones de consulta del historial de movimientos
                de inventario de los productos del catálogo.
                
                Los movimientos son generados automáticamente por
                las operaciones de gestión de stock y no se registran
                manualmente mediante este recurso.
                """
)
public interface StockMovementControllerDocs {

    @Operation(
            summary = "Obtener movimientos de stock de un producto",
            description = """
                    Recupera el historial de movimientos de inventario
                    asociados a un producto específico.
                    
                    La respuesta utiliza paginación y permite ordenar
                    los resultados mediante los parámetros de Pageable.
                    
                    Requiere autenticación y uno de los siguientes roles:
                    ADMINISTRADOR u OPERADOR.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
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
                    description = """
                            El identificador del producto no tiene un formato
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
                            Acceso denegado. El usuario no posee el rol
                            ADMINISTRADOR u OPERADOR requerido.
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
    ResponseEntity<StandardResponse<Page<StockMovementResponse>>> findByProductId(

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
