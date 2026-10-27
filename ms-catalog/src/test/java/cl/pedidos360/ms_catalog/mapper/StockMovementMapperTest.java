package cl.pedidos360.ms_catalog.mapper;

import cl.pedidos360.ms_catalog.dto.response.StockMovementResponse;
import cl.pedidos360.ms_catalog.entity.StockMovementEntity;
import cl.pedidos360.ms_catalog.enums.StockMovementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StockMovementMapperTest {

    private StockMovementMapper stockMovementMapper;

    @BeforeEach
    void setUp() {
        stockMovementMapper = new StockMovementMapper();
    }

    @Test
    void toEntityShouldMapAllProvidedFields() {
        // Arrange
        UUID productId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();

        StockMovementType type = StockMovementType.RESTOCK;

        // Act
        StockMovementEntity result = stockMovementMapper.toEntity(
                productId,
                type,
                10,
                20,
                30,
                "Reposición de inventario",
                correlationId
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getType()).isEqualTo(type);
        assertThat(result.getQuantity()).isEqualTo(10);
        assertThat(result.getPreviousStock()).isEqualTo(20);
        assertThat(result.getResultingStock()).isEqualTo(30);
        assertThat(result.getReason()).isEqualTo("Reposición de inventario");
        assertThat(result.getCorrelationId()).isEqualTo(correlationId);
    }

    @Test
    void toEntityShouldPreserveNullOptionalFields() {
        // Arrange
        UUID productId = UUID.randomUUID();

        // Act
        StockMovementEntity result = stockMovementMapper.toEntity(
                productId,
                StockMovementType.SALE,
                5,
                20,
                15,
                null,
                null
        );

        // Assert
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getType()).isEqualTo(StockMovementType.SALE);
        assertThat(result.getQuantity()).isEqualTo(5);
        assertThat(result.getPreviousStock()).isEqualTo(20);
        assertThat(result.getResultingStock()).isEqualTo(15);
        assertThat(result.getReason()).isNull();
        assertThat(result.getCorrelationId()).isNull();
    }

    @Test
    void toResponseShouldMapAllEntityFields() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();

        Instant createdAt = Instant.parse("2026-10-01T10:00:00Z");

        StockMovementEntity entity = new StockMovementEntity();
        entity.setId(id);
        entity.setProductId(productId);
        entity.setType(StockMovementType.RESTOCK);
        entity.setQuantity(10);
        entity.setPreviousStock(20);
        entity.setResultingStock(30);
        entity.setReason("Reposición de inventario");
        entity.setCorrelationId(correlationId);
        entity.setCreatedAt(createdAt);
        entity.setCreatedBy(createdBy);

        // Act
        StockMovementResponse result =
                stockMovementMapper.toResponse(entity);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.productId()).isEqualTo(productId);
        assertThat(result.type()).isEqualTo(StockMovementType.RESTOCK);
        assertThat(result.quantity()).isEqualTo(10);
        assertThat(result.previousStock()).isEqualTo(20);
        assertThat(result.resultingStock()).isEqualTo(30);
        assertThat(result.reason()).isEqualTo("Reposición de inventario");
        assertThat(result.correlationId()).isEqualTo(correlationId);
        assertThat(result.createdAt()).isEqualTo(createdAt);
        assertThat(result.createdBy()).isEqualTo(createdBy);
    }

    @Test
    void toResponseShouldPreserveNullOptionalFields() {
        // Arrange
        StockMovementEntity entity = new StockMovementEntity();
        UUID generatedId = entity.getId();

        entity.setProductId(UUID.randomUUID());
        entity.setType(StockMovementType.SALE);
        entity.setQuantity(5);
        entity.setPreviousStock(20);
        entity.setResultingStock(15);
        entity.setReason(null);
        entity.setCorrelationId(null);
        entity.setCreatedAt(null);
        entity.setCreatedBy(null);

        // Act
        StockMovementResponse result =
                stockMovementMapper.toResponse(entity);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(generatedId);
        assertThat(result.reason()).isNull();
        assertThat(result.correlationId()).isNull();
        assertThat(result.createdAt()).isNull();
        assertThat(result.createdBy()).isNull();
    }
}
