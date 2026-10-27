package cl.pedidos360.ms_catalog.repository;

import cl.pedidos360.ms_catalog.entity.ProductEntity;
import cl.pedidos360.ms_catalog.repository.custom.ProductRepositoryCustom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio principal de persistencia para los productos del catálogo.
 *
 * <p>Extiende {@link MongoRepository} para proporcionar las operaciones
 * CRUD estándar sobre {@link ProductEntity}.</p>
 *
 * <p>También extiende {@link ProductRepositoryCustom} para incorporar
 * consultas dinámicas que requieren filtros opcionales y paginación,
 * implementadas mediante {@code MongoTemplate}.</p>
 */
public interface ProductRepository extends MongoRepository<ProductEntity, UUID>, ProductRepositoryCustom {

    /**
     * Busca un producto mediante su SKU.
     *
     * @param sku código SKU del producto que se desea buscar
     * @return producto encontrado, o {@link Optional#empty()} si no existe
     */
    Optional<ProductEntity> findBySku(String sku);

    /**
     * Comprueba si ya existe un producto con el SKU indicado.
     *
     * <p>Se utiliza principalmente para evitar duplicidad de SKU
     * durante la creación de productos.</p>
     *
     * @param sku código SKU que se desea comprobar
     * @return {@code true} si existe un producto con el SKU indicado;
     *         {@code false} en caso contrario
     */
    boolean existsBySku(String sku);
}
