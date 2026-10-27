package cl.pedidos360.ms_catalog.controller;

import cl.pedidos360.ms_catalog.controller.docs.CategoryControllerDocs;
import cl.pedidos360.ms_catalog.dto.request.CategoryCreateRequest;
import cl.pedidos360.ms_catalog.dto.request.CategoryStatusUpdateRequest;
import cl.pedidos360.ms_catalog.dto.request.CategoryUpdateRequest;
import cl.pedidos360.ms_catalog.dto.response.CategoryResponse;
import cl.pedidos360.ms_catalog.dto.response.StandardResponse;
import cl.pedidos360.ms_catalog.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController implements CategoryControllerDocs {

    private final CategoryService categoryService;

    @Override
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<StandardResponse<CategoryResponse>> create(
            @Valid @RequestBody CategoryCreateRequest request
    ) {
        CategoryResponse response = categoryService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(StandardResponse.created(
                    "Categoría creada con éxito.",
                    response
                ));
    }

    @Override
    @GetMapping
    public ResponseEntity<StandardResponse<Page<CategoryResponse>>> findAll(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<CategoryResponse> response = categoryService.findAll(pageable);

        return ResponseEntity.ok(StandardResponse.ok(
                "Categorías recuperadas con éxito.",
                response
        ));
    }

    @Override
    @GetMapping("/{categoryId}")
    public ResponseEntity<StandardResponse<CategoryResponse>> findById(
            @PathVariable UUID categoryId
    ) {
        CategoryResponse response = categoryService.findById(categoryId);

        return ResponseEntity.ok(StandardResponse.ok(
                "Categoría recuperada con éxito.",
                response
        ));
    }

    @Override
    @PutMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<StandardResponse<CategoryResponse>> update(
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryUpdateRequest request
    ) {
        CategoryResponse response = categoryService.update(categoryId, request);

        return ResponseEntity.ok(StandardResponse.ok(
                "Categoría actualizada con éxito.",
                response
        ));
    }

    @Override
    @PatchMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<StandardResponse<CategoryResponse>> updateStatus(
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryStatusUpdateRequest request
    ) {
        CategoryResponse response = categoryService.updateStatus(categoryId, request);

        return ResponseEntity.ok(StandardResponse.ok(
                "El estado de la categoría se ha actualizado correctamente.",
                response
        ));
    }

    @Override
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<StandardResponse<Void>> delete(
            @PathVariable UUID categoryId
    ) {
        categoryService.delete(categoryId);

        return ResponseEntity.ok(StandardResponse.ok(
                "La categoría se ha eliminado correctamente.",
                null
        ));
    }
}
