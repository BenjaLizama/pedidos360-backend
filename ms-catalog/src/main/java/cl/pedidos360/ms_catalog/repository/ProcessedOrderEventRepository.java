package cl.pedidos360.ms_catalog.repository;

import cl.pedidos360.ms_catalog.entity.ProcessedOrderEventEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface ProcessedOrderEventRepository
        extends MongoRepository<ProcessedOrderEventEntity, UUID> {
}
