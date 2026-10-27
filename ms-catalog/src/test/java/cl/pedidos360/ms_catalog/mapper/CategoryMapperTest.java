package cl.pedidos360.ms_catalog.mapper;

import cl.pedidos360.ms_catalog.dto.request.CategoryCreateRequest;
import cl.pedidos360.ms_catalog.dto.request.CategoryUpdateRequest;
import cl.pedidos360.ms_catalog.dto.response.CategoryResponse;
import cl.pedidos360.ms_catalog.entity.CategoryEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryMapperTest {

    private CategoryMapper categoryMapper;

    @BeforeEach
    void setUp() {
        categoryMapper = new CategoryMapper();
    }

    @Test
    void toEntityShouldMapCreateRequestFields() {
        // Arrange
        CategoryCreateRequest request = new CategoryCreateRequest(
                "Electrónica",
                "Productos electrónicos"
        );

        // Act
        CategoryEntity result = categoryMapper.toEntity(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Electrónica");
        assertThat(result.getDescription())
                .isEqualTo("Productos electrónicos");
    }

    @Test
    void toEntityShouldPreserveNullDescription() {
        // Arrange
        CategoryCreateRequest request = new CategoryCreateRequest(
                "Electrónica",
                null
        );

        // Act
        CategoryEntity result = categoryMapper.toEntity(request);

        // Assert
        assertThat(result.getName()).isEqualTo("Electrónica");
        assertThat(result.getDescription()).isNull();
    }

    @Test
    void updateEntityShouldUpdateOnlyAllowedFields() {
        // Arrange
        CategoryEntity category = createCategory();

        UUID originalId = category.getId();
        boolean originalActive = category.isActive();
        Instant originalCreatedAt = category.getCreatedAt();
        Instant originalUpdatedAt = category.getUpdatedAt();

        CategoryUpdateRequest request = new CategoryUpdateRequest(
                "Hogar",
                "Productos para el hogar"
        );

        // Act
        categoryMapper.updateEntity(category, request);

        // Assert
        assertThat(category.getName()).isEqualTo("Hogar");
        assertThat(category.getDescription())
                .isEqualTo("Productos para el hogar");

        // Los campos fuera de la responsabilidad del mapper
        // deben permanecer sin modificaciones.
        assertThat(category.getId()).isEqualTo(originalId);
        assertThat(category.isActive()).isEqualTo(originalActive);
        assertThat(category.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(category.getUpdatedAt()).isEqualTo(originalUpdatedAt);
    }

    @Test
    void updateEntityShouldAllowNullDescription() {
        // Arrange
        CategoryEntity category = createCategory();

        CategoryUpdateRequest request = new CategoryUpdateRequest(
                "Hogar",
                null
        );

        // Act
        categoryMapper.updateEntity(category, request);

        // Assert
        assertThat(category.getName()).isEqualTo("Hogar");
        assertThat(category.getDescription()).isNull();
    }

    @Test
    void toResponseShouldMapAllFields() {
        // Arrange
        CategoryEntity category = createCategory();

        // Act
        CategoryResponse result = categoryMapper.toResponse(category);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(category.getId());
        assertThat(result.name()).isEqualTo(category.getName());
        assertThat(result.description())
                .isEqualTo(category.getDescription());
        assertThat(result.active()).isEqualTo(category.isActive());
        assertThat(result.createdAt()).isEqualTo(category.getCreatedAt());
        assertThat(result.updatedAt()).isEqualTo(category.getUpdatedAt());
    }

    @Test
    void toResponseShouldPreserveNullOptionalFields() {
        // Arrange
        CategoryEntity category = new CategoryEntity();
        category.setId(UUID.randomUUID());
        category.setName("Sin descripción");
        category.setDescription(null);
        category.setActive(true);

        // Act
        CategoryResponse result = categoryMapper.toResponse(category);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Sin descripción");
        assertThat(result.description()).isNull();
        assertThat(result.active()).isTrue();
        assertThat(result.createdAt()).isNull();
        assertThat(result.updatedAt()).isNull();
    }

    private CategoryEntity createCategory() {
        CategoryEntity category = new CategoryEntity();

        category.setId(UUID.randomUUID());
        category.setName("Electrónica");
        category.setDescription("Productos electrónicos");
        category.setActive(true);
        category.setCreatedAt(Instant.parse("2026-10-01T10:00:00Z"));
        category.setUpdatedAt(Instant.parse("2026-10-02T12:00:00Z"));

        return category;
    }
}
