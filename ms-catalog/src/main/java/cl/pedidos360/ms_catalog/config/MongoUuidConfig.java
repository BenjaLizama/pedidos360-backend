package cl.pedidos360.ms_catalog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;

import java.util.UUID;

/**
 * Configuración para generar UUID automáticamente antes de persistir
 * entidades que utilizan UUID como identificador.
 */
@Configuration
public class MongoUuidConfig {

    @Bean
    public BeforeConvertCallback<Object> uuidGeneratorCallback() {
        return (entity, collection) -> {

            if (entity instanceof HasUuidId uuidEntity
                    && uuidEntity.getId() == null) {

                uuidEntity.setId(UUID.randomUUID());
            }

            return entity;
        };
    }

    /**
     * Contrato interno utilizado por las entidades que requieren
     * generación automática de UUID.
     */
    public interface HasUuidId {

        UUID getId();

        void setId(UUID id);
    }
}
