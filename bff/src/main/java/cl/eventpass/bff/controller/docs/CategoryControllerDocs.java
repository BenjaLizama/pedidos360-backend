package cl.eventpass.bff.controller.docs;

import java.util.Map;
import java.util.UUID;

import cl.eventpass.bff.dto.request.CategoryCreateRequest;
import cl.eventpass.bff.dto.request.CategoryStatusUpdateRequest;
import cl.eventpass.bff.dto.request.CategoryUpdateRequest;
import cl.eventpass.bff.dto.response.CategoryResponse;
import cl.eventpass.bff.dto.response.StandardResponse;

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

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(
        name = "Categorías",
        description = """
                Operaciones para consultar y gestionar las categorías
                del catálogo mediante el Backend for Frontend (BFF).
                """
)
@SecurityRequirement(name = "bearerAuth")
public interface CategoryControllerDocs {

    @Operation(
            summary = "Crear una categoría",
            description = """
                    Envía una solicitud al microservicio de catálogo
                    para crear una nueva categoría.
                    Requiere los permisos definidos para esta operación.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Categoría creada correctamente",
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
                    description = "No autenticado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tiene permisos para crear categorías"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Ya existe una categoría con el mismo nombre"
            )
    })
    StandardResponse<CategoryResponse> create(
            @RequestBody(
                    description = "Datos de la categoría que se desea crear",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = CategoryCreateRequest.class
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody
            CategoryCreateRequest request
    );

    @Operation(
            summary = "Obtener categorías",
            description = """
                    Recupera las categorías mediante el microservicio
                    de catálogo, conservando los filtros y parámetros
                    de paginación admitidos por dicho servicio.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categorías recuperadas correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parámetros de consulta inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado"
            )
    })
    StandardResponse<?> findAll(
            @Parameter(
                    description = """
                            Filtros, paginación y ordenamiento admitidos
                            por el microservicio de catálogo.
                            """
            )
            @RequestParam Map<String, String> queryParams
    );

    @Operation(
            summary = "Obtener una categoría por ID",
            description = "Recupera una categoría mediante su identificador único."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoría recuperada correctamente",
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
                    description = "No autenticado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La categoría no existe"
            )
    })
    StandardResponse<CategoryResponse> findById(
            @PathVariable @Parameter(
                    name = "categoryId",
                    description = "Identificador único de la categoría",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            ) UUID categoryId
    );

    @Operation(
            summary = "Actualizar una categoría",
            description = "Actualiza los datos de una categoría existente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoría actualizada correctamente",
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
                    description = "No autenticado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tiene permisos para actualizar categorías"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La categoría no existe"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Ya existe otra categoría con el mismo nombre"
            )
    })
    StandardResponse<CategoryResponse> update(
            @PathVariable @Parameter(
                    name = "categoryId",
                    description = "Identificador único de la categoría",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            ) UUID categoryId,

            @RequestBody(
                    description = "Nuevos datos de la categoría",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = CategoryUpdateRequest.class
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody
            CategoryUpdateRequest request
    );

    @Operation(
            summary = "Actualizar el estado de una categoría",
            description = "Activa o desactiva una categoría existente."
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
                    description = "No autenticado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tiene permisos para modificar el estado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La categoría no existe"
            )
    })
    StandardResponse<CategoryResponse> updateStatus(
            @PathVariable @Parameter(
                    name = "categoryId",
                    description = "Identificador único de la categoría",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            ) UUID categoryId,

            @RequestBody(
                    description = "Nuevo estado de la categoría",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = CategoryStatusUpdateRequest.class
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody
            CategoryStatusUpdateRequest request
    );

    @Operation(
            summary = "Eliminar una categoría",
            description = "Solicita la eliminación lógica de una categoría."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Categoría eliminada correctamente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Identificador UUID inválido"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tiene permisos para eliminar categorías"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La categoría no existe"
            )
    })
    void delete(
            @PathVariable @Parameter(
                    name = "categoryId",
                    description = "Identificador único de la categoría",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            ) UUID categoryId
    );
}
