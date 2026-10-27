package cl.pedidos360.ms_catalog.repository;

import cl.pedidos360.ms_catalog.entity.StockMovementEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

/**
 * Repositorio de persistencia para los movimientos de inventario.
 *
 * <p>Proporciona las operaciones estándar de persistencia para
 * {@link StockMovementEntity} y permite consultar los movimientos
 * asociados a un producto de forma paginada.</p>
 *
 * <p>Los movimientos son registros de trazabilidad generados por las
 * operaciones que modifican el inventario, por lo que su creación
 * pertenece a la lógica interna del catálogo.</p>
 */
public interface StockMovementRepository extends MongoRepository<StockMovementEntity, UUID> {

    /**
     * Obtiene los movimientos de inventario asociados a un producto.
     *
     * <p>La consulta utiliza paginación para controlar el volumen de
     * información retornado cuando un producto posee un historial
     * extenso de movimientos.</p>
     *
     * @param productId identificador del producto cuyos movimientos
     *                  se desean consultar
     * @param pageable configuración de paginación y ordenamiento
     * @return página con los movimientos de inventario del producto
     */
    Page<StockMovementEntity> findByProductId(UUID productId, Pageable pageable);
}
