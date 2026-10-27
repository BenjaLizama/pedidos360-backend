package cl.pedidos360.ms_catalog.repository;

import cl.pedidos360.ms_catalog.entity.PriceHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

/**
 * Repositorio de persistencia para el historial de precios.
 *
 * <p>Proporciona las operaciones estándar de persistencia para
 * {@link PriceHistoryEntity} y permite consultar el historial asociado
 * a un producto de forma paginada.</p>
 *
 * <p>Los registros históricos son generados por la lógica de negocio
 * cuando se modifica el precio de un producto.</p>
 */
public interface PriceHistoryRepository extends MongoRepository<PriceHistoryEntity, UUID> {

    /**
     * Obtiene el historial de modificaciones de precio de un producto.
     *
     * <p>La paginación evita cargar todos los registros históricos
     * de un producto en memoria cuando existe un volumen considerable
     * de información.</p>
     *
     * @param productId identificador del producto cuyo historial se consulta
     * @param pageable configuración de paginación y ordenamiento
     * @return página con los registros históricos del producto
     */
    Page<PriceHistoryEntity> findByProductId(UUID productId, Pageable pageable);
}
