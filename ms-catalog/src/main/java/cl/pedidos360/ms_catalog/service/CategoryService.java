package cl.pedidos360.ms_catalog.service;

import cl.pedidos360.ms_catalog.dto.request.CategoryCreateRequest;
import cl.pedidos360.ms_catalog.dto.request.CategoryStatusUpdateRequest;
import cl.pedidos360.ms_catalog.dto.request.CategoryUpdateRequest;
import cl.pedidos360.ms_catalog.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Servicio encargado de gestionar las operaciones de negocio
 * relacionadas con las categorías del catálogo.
 *
 * <p>Define las operaciones de creación, consulta, actualización,
 * cambio de estado y eliminación lógica de categorías.</p>
 */
public interface CategoryService {

    /**
     * Crea una nueva categoría.
     *
     * @param request datos necesarios para crear la categoría
     * @return información de la categoría creada
     */
    CategoryResponse create(CategoryCreateRequest request);

    /**
     * Obtiene las categorías existentes de forma paginada.
     *
     * @param pageable configuración de paginación y ordenamiento
     * @return página de categorías
     */
    Page<CategoryResponse> findAll(Pageable pageable);

    /**
     * Busca una categoría mediante su identificador.
     *
     * @param id identificador único de la categoría
     * @return información de la categoría encontrada
     */
    CategoryResponse findById(UUID id);

    /**
     * Elimina lógicamente una categoría.
     *
     * <p>La eliminación no implica borrar físicamente el documento
     * de MongoDB, sino modificar su estado para conservar su historial.</p>
     *
     * @param id identificador único de la categoría
     */
    void delete(UUID id);

    /**
     * Actualiza la información descriptiva de una categoría.
     *
     * @param id identificador único de la categoría
     * @param request nuevos datos de la categoría
     * @return información actualizada de la categoría
     */
    CategoryResponse update(
            UUID id,
            CategoryUpdateRequest request
    );

    /**
     * Actualiza exclusivamente el estado de una categoría.
     *
     * @param id identificador único de la categoría
     * @param request nuevo estado de la categoría
     * @return información actualizada de la categoría
     */
    CategoryResponse updateStatus(
            UUID id,
            CategoryStatusUpdateRequest request
    );
}
