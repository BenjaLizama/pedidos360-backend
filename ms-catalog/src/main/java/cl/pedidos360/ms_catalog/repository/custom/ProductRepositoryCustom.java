package cl.pedidos360.ms_catalog.repository.custom;

import cl.pedidos360.ms_catalog.entity.ProductEntity;
import cl.pedidos360.ms_catalog.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Contrato para las consultas dinámicas de productos del catálogo.
 *
 * <p>Se utiliza para operaciones de búsqueda que requieren combinar
 * filtros opcionales y paginación, funcionalidad que no resulta
 * conveniente expresar únicamente mediante métodos derivados de
 * Spring Data MongoDB.</p>
 */
public interface ProductRepositoryCustom {

    /**
     * Busca productos aplicando únicamente los filtros que hayan sido
     * proporcionados.
     *
     * <p>Los filtros pueden combinarse para realizar búsquedas por nombre,
     * categoría y estado. La consulta resultante se ejecuta de forma
     * paginada.</p>
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
    Page<ProductEntity> search(
            String name,
            UUID categoryId,
            ProductStatus status,
            Pageable pageable
    );
}
