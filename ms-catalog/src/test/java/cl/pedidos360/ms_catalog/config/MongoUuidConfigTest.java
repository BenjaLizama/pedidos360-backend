package cl.pedidos360.ms_catalog.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MongoUuidConfigTest {

    private MongoUuidConfig config;
    private BeforeConvertCallback<Object> callback;

    @BeforeEach
    void setUp() {
        config = new MongoUuidConfig();
        callback = config.uuidGeneratorCallback();
    }

    @Test
    void shouldGenerateUuidWhenEntityIdIsNull() {
        TestUuidEntity entity = new TestUuidEntity(null);

        Object result = callback.onBeforeConvert(entity, "products");

        assertSame(entity, result);
        assertNotNull(entity.getId());
    }

    @Test
    void shouldPreserveExistingUuid() {
        UUID expectedId = UUID.randomUUID();
        TestUuidEntity entity = new TestUuidEntity(expectedId);

        Object result = callback.onBeforeConvert(entity, "products");

        assertSame(entity, result);
        assertEquals(expectedId, entity.getId());
    }

    @Test
    void shouldReturnEntityUnchangedWhenItDoesNotImplementHasUuidId() {
        Object entity = new Object();

        Object result = callback.onBeforeConvert(entity, "products");

        assertSame(entity, result);
    }

    private static class TestUuidEntity
            implements MongoUuidConfig.HasUuidId {

        private UUID id;

        private TestUuidEntity(UUID id) {
            this.id = id;
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public void setId(UUID id) {
            this.id = id;
        }
    }
}
