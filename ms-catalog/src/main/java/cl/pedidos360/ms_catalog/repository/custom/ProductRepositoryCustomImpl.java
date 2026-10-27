package cl.pedidos360.ms_catalog.repository.custom;

import cl.pedidos360.ms_catalog.entity.ProductEntity;
import cl.pedidos360.ms_catalog.enums.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Implementación de las consultas dinámicas de productos.
 *
 * <p>Utiliza {@link MongoTemplate} para construir dinámicamente la
 * consulta en función de los filtros proporcionados.</p>
 *
 * <p>La implementación permite combinar nombre, categoría y estado
 * sin necesidad de crear múltiples métodos de consulta para cada
 * combinación posible de filtros.</p>
 */
@Repository
@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    /**
     * Plantilla utilizada para construir y ejecutar consultas
     * directamente sobre MongoDB.
     */
    private final MongoTemplate mongoTemplate;

    /**
     * Ejecuta una búsqueda dinámica de productos.
     *
     * <p>Cada filtro es agregado únicamente cuando posee un valor válido.
     * Cuando se proporcionan varios filtros, estos se combinan mediante
     * una condición lógica AND.</p>
     *
     * <p>La búsqueda por nombre no distingue entre mayúsculas y minúsculas
     * y trata el texto recibido como contenido literal, evitando que los
     * caracteres especiales de una expresión regular modifiquen la consulta.</p>
     *
     * <p>La consulta obtiene el total de registros coincidentes antes de
     * aplicar la paginación, permitiendo construir correctamente un
     * {@link Page}.</p>
     *
     * @param name texto utilizado para buscar productos por nombre;
     *             puede ser {@code null} o estar vacío
     * @param categoryId identificador de la categoría utilizada como filtro;
     *                   puede ser {@code null}
     * @param status estado utilizado como filtro;
     *               puede ser {@code null}
     * @param pageable configuración de paginación y ordenamiento
     * @return página de productos que cumplen los filtros proporcionados
     */
    @Override
    public Page<ProductEntity> search(
            String name,
            UUID categoryId,
            ProductStatus status,
            Pageable pageable
    ) {
        List<Criteria> criteriaList = new ArrayList<>();

        /*
         * Agrega el filtro por nombre cuando se proporciona un valor.
         *
         * Pattern.quote() evita que caracteres especiales del texto
         * sean interpretados como operadores de expresión regular.
         *
         * El indicador "i" permite realizar la búsqueda sin distinguir
         * entre mayúsculas y minúsculas.
         */
        if (name != null && !name.isBlank()) {
            String searchTerm = Pattern.quote(name.trim());

            criteriaList.add(
                    Criteria.where("name")
                            .regex(searchTerm, "i")
            );
        }

        /*
         * Agrega el filtro por categoría cuando se proporciona
         * un identificador.
         */
        if (categoryId != null) {
            criteriaList.add(
                    Criteria.where("categoryId")
                            .is(categoryId)
            );
        }

        /*
         * Agrega el filtro por estado cuando se proporciona.
         */
        if (status != null) {
            criteriaList.add(
                    Criteria.where("status")
                            .is(status)
            );
        }

        Query query = new Query();

        /*
         * Cuando existen filtros, se combinan mediante AND para que
         * el producto deba cumplir todas las condiciones especificadas.
         */
        if (!criteriaList.isEmpty()) {
            query.addCriteria(
                    new Criteria().andOperator(
                            criteriaList.toArray(new Criteria[0])
                    )
            );
        }

        /*
         * Obtiene la cantidad total de documentos que cumplen los filtros.
         *
         * Este valor permite construir correctamente la información
         * de paginación del objeto Page.
         */
        long total = mongoTemplate.count(
                query,
                ProductEntity.class
        );

        /*
         * Aplica la paginación y el ordenamiento definidos por el cliente.
         */
        query.with(pageable);

        /*
         * Ejecuta la consulta paginada.
         */
        List<ProductEntity> products = mongoTemplate.find(
                query,
                ProductEntity.class
        );

        return new PageImpl<>(
                products,
                pageable,
                total
        );
    }
}
