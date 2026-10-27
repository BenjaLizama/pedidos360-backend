package cl.pedidos360.ms_catalog.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class MongoConfigTest {

    @Test
    void shouldCreateMongoTransactionManager() {
        MongoConfig config = new MongoConfig();
        MongoDatabaseFactory databaseFactory =
                mock(MongoDatabaseFactory.class);

        MongoTransactionManager result =
                config.transactionManager(databaseFactory);

        assertNotNull(result);
    }
}
