package cl.eventpass.bff.controller;

import cl.eventpass.bff.client.CatalogClient;
import cl.eventpass.bff.controller.docs.CategoryControllerDocs;
import cl.eventpass.bff.dto.request.CategoryCreateRequest;
import cl.eventpass.bff.dto.request.CategoryStatusUpdateRequest;
import cl.eventpass.bff.dto.request.CategoryUpdateRequest;
import cl.eventpass.bff.dto.response.CategoryResponse;
import cl.eventpass.bff.dto.response.StandardResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController implements CategoryControllerDocs {

    private final CatalogClient catalogClient;

    /**
     * Crea una categoría mediante el microservicio de catálogo.
     */
    @Override
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public StandardResponse<CategoryResponse> create(
            @Valid @RequestBody CategoryCreateRequest request
    ) {
        return catalogClient.createCategory(request);
    }

    /**
     * Obtiene las categorías y reenvía los parámetros de consulta.
     */
    @Override
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public StandardResponse<?> findAll(
            @RequestParam Map<String, String> queryParams
    ) {
        return catalogClient.getCategories(queryParams);
    }

    /**
     * Busca una categoría por su identificador.
     */
    @Override
    @GetMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    public StandardResponse<CategoryResponse> findById(
            @PathVariable UUID categoryId
    ) {
        return catalogClient.getCategoryById(categoryId);
    }

    /**
     * Actualiza los datos de una categoría.
     */
    @Override
    @PutMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    @ResponseStatus(HttpStatus.OK)
    public StandardResponse<CategoryResponse> update(
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryUpdateRequest request
    ) {
        return catalogClient.updateCategory(categoryId, request);
    }

    /**
     * Actualiza el estado de una categoría.
     */
    @Override
    @PatchMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    @ResponseStatus(HttpStatus.OK)
    public StandardResponse<CategoryResponse> updateStatus(
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryStatusUpdateRequest request
    ) {
        return catalogClient.updateCategoryStatus(categoryId, request);
    }

    /**
     * Elimina una categoría mediante el microservicio de catálogo.
     */
    @Override
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID categoryId
    ) {
        catalogClient.deleteCategory(categoryId);
    }
}
