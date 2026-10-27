package cl.pedidos360.ms_catalog.service.impl;

import cl.pedidos360.ms_catalog.dto.request.CategoryCreateRequest;
import cl.pedidos360.ms_catalog.dto.request.CategoryStatusUpdateRequest;
import cl.pedidos360.ms_catalog.dto.request.CategoryUpdateRequest;
import cl.pedidos360.ms_catalog.dto.response.CategoryResponse;
import cl.pedidos360.ms_catalog.entity.CategoryEntity;
import cl.pedidos360.ms_catalog.exception.ResourceAlreadyExistsException;
import cl.pedidos360.ms_catalog.exception.ResourceNotFoundException;
import cl.pedidos360.ms_catalog.mapper.CategoryMapper;
import cl.pedidos360.ms_catalog.observability.CorrelationIdProvider;
import cl.pedidos360.ms_catalog.repository.CategoryRepository;
import cl.pedidos360.ms_catalog.security.CurrentUserProvider;
import cl.pedidos360.ms_catalog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implementación del servicio encargado de gestionar las categorías
 * del catálogo.
 *
 * <p>Centraliza las reglas de negocio relacionadas con la creación,
 * consulta, actualización, cambio de estado y eliminación lógica
 * de categorías.</p>
 *
 * <p>Las operaciones relevantes se registran mediante SLF4J
 * utilizando el usuario autenticado y el correlation ID.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final CurrentUserProvider currentUserProvider;
    private final CorrelationIdProvider correlationIdProvider;

    /**
     * Crea una nueva categoría.
     *
     * <p>No permite registrar categorías con un nombre que ya exista,
     * ignorando diferencias entre mayúsculas y minúsculas.</p>
     *
     * @param request datos necesarios para crear la categoría
     * @return información de la categoría creada
     * @throws ResourceAlreadyExistsException si ya existe una categoría
     * con el mismo nombre
     */
    @Override
    public CategoryResponse create(CategoryCreateRequest request) {
        UUID userId = currentUserProvider.getCurrentUserId();
        UUID correlationId = correlationIdProvider.getCorrelationId();

        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            log.warn(
                    "No se pudo crear la categoría porque el nombre ya existe " +
                            "| userId={} | categoryName={} | correlationId={}",
                    userId,
                    request.name(),
                    correlationId
            );

            throw new ResourceAlreadyExistsException(
                    "Ya existe una categoría con el nombre: "
                            + request.name()
            );
        }

        CategoryEntity category = categoryMapper.toEntity(request);
        CategoryEntity savedCategory = categoryRepository.save(category);

        log.info(
                "Categoría creada correctamente " +
                "| userId={} | categoryId={} | correlationId={}",
                userId,
                savedCategory.getId(),
                correlationId
        );

        return categoryMapper.toResponse(savedCategory);
    }

    /**
     * Obtiene las categorías disponibles utilizando paginación.
     *
     * @param pageable configuración de paginación y ordenamiento
     * @return página de categorías
     */
    @Override
    public Page<CategoryResponse> findAll(Pageable pageable) {
        UUID correlationId = correlationIdProvider.getCorrelationId();

        log.debug(
                "Consultando categorías | page={} | size={} | correlationId={}",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                correlationId
        );

        return categoryRepository
                .findAll(pageable)
                .map(categoryMapper::toResponse);
    }

    /**
     * Obtiene una categoría mediante su identificador.
     *
     * @param id identificador de la categoría
     * @return información de la categoría encontrada
     * @throws ResourceNotFoundException si la categoría no existe
     */
    @Override
    public CategoryResponse findById(UUID id) {
        UUID correlationId = correlationIdProvider.getCorrelationId();
        CategoryEntity category = findCategoryById(id);

        log.debug(
                "Categoría encontrada | categoryId={} | correlationId={}",
                id,
                correlationId
        );

        return categoryMapper.toResponse(category);
    }

    /**
     * Realiza la eliminación lógica de una categoría.
     *
     * <p>La categoría no se elimina físicamente de MongoDB.
     * Su estado se establece como inactivo.</p>
     *
     * @param id identificador de la categoría
     * @throws ResourceNotFoundException si la categoría no existe
     */
    @Override
    public void delete(UUID id) {
        UUID userId = currentUserProvider.getCurrentUserId();
        UUID correlationId = correlationIdProvider.getCorrelationId();

        CategoryEntity category = findCategoryById(id);

        category.setActive(false);

        categoryRepository.save(category);

        log.info(
                "Categoría eliminada lógicamente " +
                "| userId={} | categoryId={} | correlationId={}",
                userId,
                id,
                correlationId
        );
    }

    /**
     * Actualiza los datos generales de una categoría.
     *
     * <p>No permite modificar la categoría si el nuevo nombre
     * ya pertenece a otra categoría.</p>
     *
     * @param id identificador de la categoría
     * @param request nuevos datos de la categoría
     * @return información de la categoría actualizada
     * @throws ResourceNotFoundException si la categoría no existe
     * @throws ResourceAlreadyExistsException si el nuevo nombre
     * ya está registrado
     */
    @Override
    public CategoryResponse update(UUID id, CategoryUpdateRequest request) {
        UUID userId = currentUserProvider.getCurrentUserId();
        UUID correlationId = correlationIdProvider.getCorrelationId();

        CategoryEntity category = findCategoryById(id);

        if (!category.getName().equalsIgnoreCase(request.name())
            && categoryRepository.existsByNameIgnoreCase(request.name())
        ) {
            log.warn(
                    "No se pudo actualizar la categoría porque " +
                            "el nombre ya existe | userId={} | categoryId={} | name={} | correlationId={}",
                    userId,
                    id,
                    request.name(),
                    correlationId
            );

            throw new ResourceAlreadyExistsException(
                    "Ya existe una categoría con el nombre: "
                            + request.name()
            );
        }

        categoryMapper.updateEntity(
                category,
                request
        );

        CategoryEntity updatedCategory = categoryRepository.save(category);

        log.info(
                "Categoría actualizada correctamente " +
                "| userId={} | categoryId={} | correlationId={}",
                userId,
                updatedCategory.getId(),
                correlationId
        );

        return categoryMapper.toResponse(updatedCategory);
    }

    /**
     * Actualiza el estado activo de una categoría.
     *
     * <p>Permite activar o desactivar una categoría sin eliminar
     * físicamente el documento.</p>
     *
     * @param id identificador de la categoría
     * @param request nuevo estado de la categoría
     * @return información de la categoría actualizada
     * @throws ResourceNotFoundException si la categoría no existe
     */
    @Override
    public CategoryResponse updateStatus(
            UUID id,
            CategoryStatusUpdateRequest request
    ) {
        UUID userId =
                currentUserProvider.getCurrentUserId();

        UUID correlationId =
                correlationIdProvider.getCorrelationId();

        CategoryEntity category =
                findCategoryById(id);

        boolean previousStatus =
                category.isActive();

        category.setActive(
                request.active()
        );

        CategoryEntity updatedCategory =
                categoryRepository.save(category);

        log.info(
                "Estado de categoría actualizado " +
                        "| userId={} | categoryId={} | previousActive={} " +
                        "| active={} | correlationId={}",
                userId,
                updatedCategory.getId(),
                previousStatus,
                updatedCategory.isActive(),
                correlationId
        );

        return categoryMapper.toResponse(
                updatedCategory
        );
    }

    /**
     * Busca una categoría por su identificador.
     *
     * @param id identificador de la categoría
     * @return entidad encontrada
     * @throws ResourceNotFoundException si la categoría no existe
     */
    private CategoryEntity findCategoryById(UUID id) {
        UUID correlationId = correlationIdProvider.getCorrelationId();

        return categoryRepository
                .findById(id)
                .orElseThrow(() -> {
                    log.warn(
                            "Categoría no encontrada | categoryId={} | correlationId={}",
                            id,
                            correlationId
                    );

                    return new ResourceNotFoundException(
                            "No se encontró la categoría con ID: "
                                    + id
                    );
                });
    }
}
