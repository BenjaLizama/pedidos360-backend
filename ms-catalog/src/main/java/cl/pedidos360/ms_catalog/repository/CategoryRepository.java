package cl.pedidos360.ms_catalog.repository;

import cl.pedidos360.ms_catalog.entity.CategoryEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de persistencia para las categorías del catálogo.
 *
 * <p>Extiende {@link MongoRepository} para proporcionar las operaciones
 * CRUD estándar sobre documentos {@link CategoryEntity}.</p>
 *
 * <p>Los métodos adicionales permiten validar la existencia de categorías
 * mediante su nombre sin necesidad de implementar consultas manualmente.</p>
 */
public interface CategoryRepository extends MongoRepository<CategoryEntity, UUID> {

    /**
     * Busca una categoría utilizando su nombre ignorando diferencias
     * entre mayúsculas y minúsculas.
     *
     * @param name nombre de la categoría que se desea buscar
     * @return categoría encontrada, o {@link Optional#empty()} si no existe
     */
    Optional<CategoryEntity> findByNameIgnoreCase(String name);

    /**
     * Comprueba si existe una categoría con el nombre indicado,
     * ignorando diferencias entre mayúsculas y minúsculas.
     *
     * @param name nombre de la categoría que se desea comprobar
     * @return {@code true} si existe una categoría con ese nombre;
     *         {@code false} en caso contrario
     */
    boolean existsByNameIgnoreCase(String name);
}
