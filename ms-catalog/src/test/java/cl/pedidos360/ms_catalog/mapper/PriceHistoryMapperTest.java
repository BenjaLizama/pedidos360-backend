package cl.pedidos360.ms_catalog.mapper;

import cl.pedidos360.ms_catalog.dto.response.PriceHistoryResponse;
import cl.pedidos360.ms_catalog.entity.PriceHistoryEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PriceHistoryMapperTest {

    private PriceHistoryMapper priceHistoryMapper;

    @BeforeEach
    void setUp() {
        priceHistoryMapper = new PriceHistoryMapper();
    }

    @Test
    void toEntityShouldMapAllProvidedFields() {
        // Arrange
        UUID productId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        BigDecimal previousPrice = new BigDecimal("9990.00");
        BigDecimal newPrice = new BigDecimal("11990.00");
        String reason = "Actualización de precio";

        // Act
        PriceHistoryEntity result = priceHistoryMapper.toEntity(
                productId,
                previousPrice,
                newPrice,
                reason,
                correlationId
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getPreviousPrice()).isEqualByComparingTo(previousPrice);
        assertThat(result.getNewPrice()).isEqualByComparingTo(newPrice);
        assertThat(result.getReason()).isEqualTo(reason);
        assertThat(result.getCorrelationId()).isEqualTo(correlationId);
    }

    @Test
    void toEntityShouldPreserveNullOptionalFields() {
        // Arrange
        UUID productId = UUID.randomUUID();

        // Act
        PriceHistoryEntity result = priceHistoryMapper.toEntity(
                productId,
                null,
                new BigDecimal("11990.00"),
                null,
                null
        );

        // Assert
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getPreviousPrice()).isNull();
        assertThat(result.getNewPrice())
                .isEqualByComparingTo("11990.00");
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

        PriceHistoryEntity entity = new PriceHistoryEntity();
        entity.setId(id);
        entity.setProductId(productId);
        entity.setPreviousPrice(new BigDecimal("9990.00"));
        entity.setNewPrice(new BigDecimal("11990.00"));
        entity.setReason("Actualización de precio");
        entity.setCorrelationId(correlationId);
        entity.setCreatedAt(createdAt);
        entity.setCreatedBy(createdBy);

        // Act
        PriceHistoryResponse result =
                priceHistoryMapper.toResponse(entity);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.productId()).isEqualTo(productId);
        assertThat(result.previousPrice())
                .isEqualByComparingTo("9990.00");
        assertThat(result.newPrice())
                .isEqualByComparingTo("11990.00");
        assertThat(result.reason()).isEqualTo("Actualización de precio");
        assertThat(result.correlationId()).isEqualTo(correlationId);
        assertThat(result.createdAt()).isEqualTo(createdAt);
        assertThat(result.createdBy()).isEqualTo(createdBy);
    }

    @Test
    void toResponseShouldPreserveNullFields() {
        // Arrange
        PriceHistoryEntity entity = new PriceHistoryEntity();
        UUID generatedId = entity.getId();

        entity.setProductId(UUID.randomUUID());
        entity.setPreviousPrice(null);
        entity.setNewPrice(new BigDecimal("11990.00"));
        entity.setReason(null);
        entity.setCorrelationId(null);
        entity.setCreatedAt(null);
        entity.setCreatedBy(null);

        // Act
        PriceHistoryResponse result =
                priceHistoryMapper.toResponse(entity);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(generatedId);
        assertThat(result.previousPrice()).isNull();
        assertThat(result.newPrice())
                .isEqualByComparingTo("11990.00");
        assertThat(result.reason()).isNull();
        assertThat(result.correlationId()).isNull();
        assertThat(result.createdAt()).isNull();
        assertThat(result.createdBy()).isNull();
    }
}
