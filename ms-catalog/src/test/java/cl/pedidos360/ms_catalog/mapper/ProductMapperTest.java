package cl.pedidos360.ms_catalog.mapper;

import cl.pedidos360.ms_catalog.dto.request.ProductCreateRequest;
import cl.pedidos360.ms_catalog.dto.request.ProductUpdateRequest;
import cl.pedidos360.ms_catalog.dto.response.ProductResponse;
import cl.pedidos360.ms_catalog.entity.ProductEntity;
import cl.pedidos360.ms_catalog.enums.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        productMapper = new ProductMapper();
    }

    @Test
    void toEntityShouldMapAllCreateRequestFields() {
        // Arrange
        UUID categoryId = UUID.randomUUID();

        ProductCreateRequest request = new ProductCreateRequest(
                "SKU-001",
                "Teclado mecánico",
                "Teclado mecánico RGB",
                new BigDecimal("29990.00"),
                25,
                categoryId
        );

        // Act
        ProductEntity result = productMapper.toEntity(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSku()).isEqualTo("SKU-001");
        assertThat(result.getName()).isEqualTo("Teclado mecánico");
        assertThat(result.getDescription())
                .isEqualTo("Teclado mecánico RGB");
        assertThat(result.getPrice())
                .isEqualByComparingTo("29990.00");
        assertThat(result.getStock()).isEqualTo(25);
        assertThat(result.getCategoryId()).isEqualTo(categoryId);
    }

    @Test
    void toEntityShouldPreserveNullOptionalFields() {
        // Arrange
        ProductCreateRequest request = new ProductCreateRequest(
                "SKU-002",
                "Mouse",
                null,
                new BigDecimal("14990.00"),
                10,
                null
        );

        // Act
        ProductEntity result = productMapper.toEntity(request);

        // Assert
        assertThat(result.getSku()).isEqualTo("SKU-002");
        assertThat(result.getName()).isEqualTo("Mouse");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getPrice())
                .isEqualByComparingTo("14990.00");
        assertThat(result.getStock()).isEqualTo(10);
        assertThat(result.getCategoryId()).isNull();
    }

    @Test
    void updateEntityShouldUpdateOnlyAllowedFields() {
        // Arrange
        ProductEntity product = createProduct();

        UUID originalId = product.getId();
        String originalSku = product.getSku();
        BigDecimal originalPrice = product.getPrice();
        Integer originalStock = product.getStock();
        ProductStatus originalStatus = product.getStatus();
        Instant originalCreatedAt = product.getCreatedAt();
        Instant originalUpdatedAt = product.getUpdatedAt();

        UUID newCategoryId = UUID.randomUUID();

        ProductUpdateRequest request = new ProductUpdateRequest(
                "Monitor gaming",
                "Monitor de 27 pulgadas",
                newCategoryId
        );

        // Act
        productMapper.updateEntity(product, request);

        // Assert
        assertThat(product.getName()).isEqualTo("Monitor gaming");
        assertThat(product.getDescription())
                .isEqualTo("Monitor de 27 pulgadas");
        assertThat(product.getCategoryId()).isEqualTo(newCategoryId);

        // Campos que no deben modificarse en esta operación.
        assertThat(product.getId()).isEqualTo(originalId);
        assertThat(product.getSku()).isEqualTo(originalSku);
        assertThat(product.getPrice()).isEqualByComparingTo(originalPrice);
        assertThat(product.getStock()).isEqualTo(originalStock);
        assertThat(product.getStatus()).isEqualTo(originalStatus);
        assertThat(product.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(product.getUpdatedAt()).isEqualTo(originalUpdatedAt);
    }

    @Test
    void updateEntityShouldAllowNullDescriptionAndCategory() {
        // Arrange
        ProductEntity product = createProduct();

        ProductUpdateRequest request = new ProductUpdateRequest(
                "Mouse actualizado",
                null,
                null
        );

        // Act
        productMapper.updateEntity(product, request);

        // Assert
        assertThat(product.getName()).isEqualTo("Mouse actualizado");
        assertThat(product.getDescription()).isNull();
        assertThat(product.getCategoryId()).isNull();
    }

    @Test
    void toResponseShouldMapAllPublicFields() {
        // Arrange
        ProductEntity product = createProduct();

        // Act
        ProductResponse result = productMapper.toResponse(product);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(product.getId());
        assertThat(result.sku()).isEqualTo(product.getSku());
        assertThat(result.name()).isEqualTo(product.getName());
        assertThat(result.description()).isEqualTo(product.getDescription());
        assertThat(result.price()).isEqualByComparingTo(product.getPrice());
        assertThat(result.stock()).isEqualTo(product.getStock());
        assertThat(result.categoryId()).isEqualTo(product.getCategoryId());
        assertThat(result.status()).isEqualTo(product.getStatus());
        assertThat(result.createdAt()).isEqualTo(product.getCreatedAt());
        assertThat(result.updatedAt()).isEqualTo(product.getUpdatedAt());
    }

    @Test
    void toResponseShouldPreserveNullOptionalFields() {
        // Arrange
        ProductEntity product = new ProductEntity();
        UUID generatedId = product.getId();

        product.setSku("SKU-003");
        product.setName("Producto sin descripción");
        product.setDescription(null);
        product.setPrice(new BigDecimal("4990.00"));
        product.setStock(0);
        product.setCategoryId(null);
        product.setStatus(ProductStatus.ACTIVE);
        product.setCreatedAt(null);
        product.setUpdatedAt(null);

        // Act
        ProductResponse result = productMapper.toResponse(product);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(generatedId);
        assertThat(result.description()).isNull();
        assertThat(result.categoryId()).isNull();
        assertThat(result.createdAt()).isNull();
        assertThat(result.updatedAt()).isNull();
        assertThat(result.stock()).isZero();
    }

    private ProductEntity createProduct() {
        ProductEntity product = new ProductEntity();

        product.setId(UUID.randomUUID());
        product.setSku("SKU-001");
        product.setName("Teclado mecánico");
        product.setDescription("Teclado mecánico RGB");
        product.setPrice(new BigDecimal("29990.00"));
        product.setStock(25);
        product.setCategoryId(UUID.randomUUID());
        product.setStatus(ProductStatus.ACTIVE);
        product.setCreatedAt(Instant.parse("2026-10-01T10:00:00Z"));
        product.setUpdatedAt(Instant.parse("2026-10-02T12:00:00Z"));

        return product;
    }
}
