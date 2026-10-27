package cl.pedidos360.ms_catalog.controller.docs;

import cl.pedidos360.ms_catalog.dto.request.CategoryCreateRequest;
import cl.pedidos360.ms_catalog.dto.request.CategoryStatusUpdateRequest;
import cl.pedidos360.ms_catalog.dto.request.CategoryUpdateRequest;
import cl.pedidos360.ms_catalog.dto.response.CategoryResponse;
import cl.pedidos360.ms_catalog.dto.response.StandardResponse;
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
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(
        name = "Categorías",
        description = """
                Operaciones para la gestión de categorías del catálogo.
                
                Las consultas de categorías son públicas.
                Las operaciones de creación y modificación requieren
                los roles ADMINISTRADOR u OPERADOR.
                La eliminación lógica requiere el rol ADMINISTRADOR.
                """
)
public interface CategoryControllerDocs {

    @Operation(
            summary = "Crear una categoría",
            description = """
                    Crea una nueva categoría en el catálogo.
                    
                    El nombre de la categoría debe ser único,
                    ignorando diferencias entre mayúsculas y minúsculas.
                    
                    Requiere autenticación y uno de los siguientes roles:
                    ADMINISTRADOR u OPERADOR.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
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
                    description = "Datos de entrada inválidos",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tiene permisos para crear categorías",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Ya existe una categoría con el mismo nombre",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            )
    })
    ResponseEntity<StandardResponse<CategoryResponse>> create(

            @RequestBody(
                    description = "Datos de la categoría que se desea crear",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = CategoryCreateRequest.class
                            )
                    )
            )
            CategoryCreateRequest request
    );


    @Operation(
            summary = "Obtener categorías",
            description = """
                    Obtiene una lista paginada de categorías.
                    
                    El endpoint es público y no requiere autenticación.
                    
                    Actualmente devuelve todas las categorías registradas,
                    incluyendo categorías activas e inactivas.
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
                    description = "Parámetros de paginación inválidos",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            )
    })
    ResponseEntity<StandardResponse<Page<CategoryResponse>>> findAll(

            @ParameterObject
            Pageable pageable
    );


    @Operation(
            summary = "Obtener una categoría por ID",
            description = """
                    Obtiene la información de una categoría mediante
                    su identificador único.
                    
                    El endpoint es público y no requiere autenticación.
                    """
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
                    description = "El identificador proporcionado no tiene un formato UUID válido",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La categoría no existe",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            )
    })
    ResponseEntity<StandardResponse<CategoryResponse>> findById(

            @Parameter(
                    name = "categoryId",
                    description = "Identificador único de la categoría",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID categoryId
    );


    @Operation(
            summary = "Actualizar una categoría",
            description = """
                    Actualiza los datos generales de una categoría.
                    
                    El nombre continúa sujeto a la regla de unicidad,
                    ignorando diferencias entre mayúsculas y minúsculas.
                    
                    Requiere autenticación y uno de los siguientes roles:
                    ADMINISTRADOR u OPERADOR.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
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
                    description = "Datos de entrada inválidos",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tiene permisos para actualizar categorías",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La categoría no existe",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Ya existe otra categoría con el mismo nombre",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            )
    })
    ResponseEntity<StandardResponse<CategoryResponse>> update(

            @Parameter(
                    name = "categoryId",
                    description = "Identificador único de la categoría",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID categoryId,

            @RequestBody(
                    description = "Nuevos datos de la categoría",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = CategoryUpdateRequest.class
                            )
                    )
            )
            CategoryUpdateRequest request
    );


    @Operation(
            summary = "Actualizar estado de una categoría",
            description = """
                    Activa o desactiva una categoría.
                    
                    Esta operación no elimina físicamente el documento
                    de MongoDB, sino que modifica su estado activo.
                    
                    Requiere autenticación y uno de los siguientes roles:
                    ADMINISTRADOR u OPERADOR.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado de la categoría actualizado correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tiene permisos para modificar el estado",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La categoría no existe",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            )
    })
    ResponseEntity<StandardResponse<CategoryResponse>> updateStatus(

            @Parameter(
                    name = "categoryId",
                    description = "Identificador único de la categoría",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID categoryId,

            @RequestBody(
                    description = "Nuevo estado de la categoría",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = CategoryStatusUpdateRequest.class
                            )
                    )
            )
            CategoryStatusUpdateRequest request
    );


    @Operation(
            summary = "Eliminar una categoría",
            description = """
                    Realiza la eliminación lógica de una categoría.
                    
                    La categoría no se elimina físicamente de MongoDB.
                    Su estado se establece como inactivo.
                    
                    Esta operación requiere autenticación y el rol
                    ADMINISTRADOR.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoría eliminada correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El identificador proporcionado no tiene un formato UUID válido",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Solo ADMINISTRADOR puede eliminar categorías",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La categoría no existe",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            )
    })
    ResponseEntity<StandardResponse<Void>> delete(

            @Parameter(
                    name = "categoryId",
                    description = "Identificador único de la categoría",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            UUID categoryId
    );
}
