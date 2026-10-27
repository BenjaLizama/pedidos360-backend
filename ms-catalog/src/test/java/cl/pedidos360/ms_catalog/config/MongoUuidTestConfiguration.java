package cl.pedidos360.ms_catalog.config;

import org.bson.UuidRepresentation;
import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
class MongoUuidTestConfiguration {

    @Bean
    MongoClientSettingsBuilderCustomizer mongoUuidCustomizer() {
        return builder -> builder.uuidRepresentation(
                UuidRepresentation.STANDARD
        );
    }
}
