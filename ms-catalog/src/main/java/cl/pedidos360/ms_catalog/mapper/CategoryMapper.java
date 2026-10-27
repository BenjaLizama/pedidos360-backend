package cl.pedidos360.ms_catalog.mapper;

import cl.pedidos360.ms_catalog.dto.request.CategoryCreateRequest;
import cl.pedidos360.ms_catalog.dto.request.CategoryUpdateRequest;
import cl.pedidos360.ms_catalog.dto.response.CategoryResponse;
import cl.pedidos360.ms_catalog.entity.CategoryEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper responsable de transformar objetos relacionados con categorías.
 * <p>
 * No contiene reglas de negocio ni lógica de persistencia.
 */
@Component
public class CategoryMapper {

    /**
     * Convierte un request de creación en una nueva entidad.
     * <p>
     * Los campos generados por el sistema, como ID, estado,
     * auditoría y versión, no se establecen aquí.
     */
    public CategoryEntity toEntity(CategoryCreateRequest request) {

        CategoryEntity category = new CategoryEntity();

        category.setName(request.name());
        category.setDescription(request.description());

        return category;
    }

    /**
     * Actualiza una categoría existente utilizando únicamente
     * los campos permitidos por la operación de actualización.
     */
    public void updateEntity(
            CategoryEntity category,
            CategoryUpdateRequest request
    ) {

        category.setName(request.name());
        category.setDescription(request.description());
    }

    /**
     * Convierte una entidad de categoría en su DTO de respuesta.
     */
    public CategoryResponse toResponse(CategoryEntity category) {

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
