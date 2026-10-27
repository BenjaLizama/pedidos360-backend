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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private CorrelationIdProvider correlationIdProvider;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private UUID categoryId;
    private UUID userId;
    private UUID correlationId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        userId = UUID.randomUUID();
        correlationId = UUID.randomUUID();

        lenient()
                .when(currentUserProvider.getCurrentUserId())
                .thenReturn(userId);

        lenient()
                .when(correlationIdProvider.getCorrelationId())
                .thenReturn(correlationId);
    }

    // ---------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------

    @Test
    void create_shouldSaveCategoryWhenNameDoesNotExist() {
        CategoryCreateRequest request =
                mock(CategoryCreateRequest.class);

        CategoryEntity category = mock(CategoryEntity.class);
        CategoryEntity savedCategory = mock(CategoryEntity.class);
        CategoryResponse expectedResponse =
                mock(CategoryResponse.class);

        when(request.name()).thenReturn("Electrónica");

        when(categoryRepository.existsByNameIgnoreCase("Electrónica"))
                .thenReturn(false);

        when(categoryMapper.toEntity(request))
                .thenReturn(category);

        when(categoryRepository.save(category))
                .thenReturn(savedCategory);

        when(savedCategory.getId())
                .thenReturn(categoryId);

        when(categoryMapper.toResponse(savedCategory))
                .thenReturn(expectedResponse);

        CategoryResponse result = categoryService.create(request);

        assertSame(expectedResponse, result);

        verify(categoryRepository)
                .existsByNameIgnoreCase("Electrónica");
        verify(categoryRepository).save(category);
        verify(categoryMapper).toEntity(request);
        verify(categoryMapper).toResponse(savedCategory);
    }

    @Test
    void create_shouldThrowWhenNameAlreadyExists() {
        CategoryCreateRequest request =
                mock(CategoryCreateRequest.class);

        when(request.name()).thenReturn("Electrónica");

        when(categoryRepository.existsByNameIgnoreCase("Electrónica"))
                .thenReturn(true);

        assertThrows(
                ResourceAlreadyExistsException.class,
                () -> categoryService.create(request)
        );

        verify(categoryRepository, never())
                .save(any(CategoryEntity.class));

        verifyNoInteractions(categoryMapper);
    }

    // ---------------------------------------------------------
    // FIND ALL
    // ---------------------------------------------------------

    @Test
    void findAll_shouldReturnMappedPage() {
        PageRequest pageable = PageRequest.of(0, 10);

        CategoryEntity category = mock(CategoryEntity.class);
        CategoryResponse expectedResponse =
                mock(CategoryResponse.class);

        Page<CategoryEntity> categoryPage =
                new PageImpl<>(List.of(category), pageable, 1);

        when(categoryRepository.findAll(pageable))
                .thenReturn(categoryPage);

        when(categoryMapper.toResponse(category))
                .thenReturn(expectedResponse);

        Page<CategoryResponse> result =
                categoryService.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertSame(expectedResponse, result.getContent().get(0));

        verify(categoryRepository).findAll(pageable);
        verify(categoryMapper).toResponse(category);
    }

    @Test
    void findAll_shouldReturnEmptyPageWhenThereAreNoCategories() {
        PageRequest pageable = PageRequest.of(0, 10);

        when(categoryRepository.findAll(pageable))
                .thenReturn(Page.empty(pageable));

        Page<CategoryResponse> result =
                categoryService.findAll(pageable);

        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());

        verify(categoryRepository).findAll(pageable);
        verifyNoInteractions(categoryMapper);
    }

    // ---------------------------------------------------------
    // FIND BY ID
    // ---------------------------------------------------------

    @Test
    void findById_shouldReturnCategoryWhenItExists() {
        CategoryEntity category = mock(CategoryEntity.class);
        CategoryResponse expectedResponse =
                mock(CategoryResponse.class);

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(categoryMapper.toResponse(category))
                .thenReturn(expectedResponse);

        CategoryResponse result =
                categoryService.findById(categoryId);

        assertSame(expectedResponse, result);

        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper).toResponse(category);
    }

    @Test
    void findById_shouldThrowWhenCategoryDoesNotExist() {
        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.findById(categoryId)
        );

        verify(categoryMapper, never())
                .toResponse(any(CategoryEntity.class));
    }

    // ---------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------

    @Test
    void update_shouldSaveChangesWhenNewNameDoesNotExist() {
        CategoryUpdateRequest request =
                mock(CategoryUpdateRequest.class);

        CategoryEntity category = mock(CategoryEntity.class);

        CategoryResponse expectedResponse =
                mock(CategoryResponse.class);

        when(request.name()).thenReturn("Deportes");

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(category.getName()).thenReturn("Electrónica");

        when(categoryRepository.existsByNameIgnoreCase("Deportes"))
                .thenReturn(false);

        when(categoryRepository.save(category))
                .thenReturn(category);

        when(categoryMapper.toResponse(category))
                .thenReturn(expectedResponse);

        CategoryResponse result =
                categoryService.update(categoryId, request);

        assertSame(expectedResponse, result);

        verify(categoryRepository)
                .existsByNameIgnoreCase("Deportes");

        verify(categoryMapper)
                .updateEntity(category, request);

        verify(categoryRepository).save(category);

        verify(categoryMapper).toResponse(category);
    }

    @Test
    void update_shouldThrowWhenNewNameBelongsToAnotherCategory() {
        CategoryUpdateRequest request =
                mock(CategoryUpdateRequest.class);

        CategoryEntity category = mock(CategoryEntity.class);

        when(request.name()).thenReturn("Deportes");

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(category.getName()).thenReturn("Electrónica");

        when(categoryRepository.existsByNameIgnoreCase("Deportes"))
                .thenReturn(true);

        assertThrows(
                ResourceAlreadyExistsException.class,
                () -> categoryService.update(categoryId, request)
        );

        verify(categoryRepository, never())
                .save(any(CategoryEntity.class));

        verify(categoryMapper, never())
                .updateEntity(any(CategoryEntity.class), any());

        verify(categoryMapper, never())
                .toResponse(any(CategoryEntity.class));
    }

    @Test
    void update_shouldThrowWhenCategoryDoesNotExist() {
        CategoryUpdateRequest request =
                mock(CategoryUpdateRequest.class);

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.update(categoryId, request)
        );

        verify(categoryRepository, never())
                .save(any(CategoryEntity.class));

        verify(categoryMapper, never())
                .updateEntity(any(CategoryEntity.class), any());
    }

    // ---------------------------------------------------------
    // UPDATE STATUS
    // ---------------------------------------------------------

    @Test
    void updateStatus_shouldChangeActiveState() {
        CategoryStatusUpdateRequest request =
                mock(CategoryStatusUpdateRequest.class);

        CategoryEntity category = mock(CategoryEntity.class);
        CategoryResponse expectedResponse =
                mock(CategoryResponse.class);

        when(request.active()).thenReturn(false);

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(category.isActive()).thenReturn(true, false);

        when(categoryRepository.save(category))
                .thenReturn(category);

        when(categoryMapper.toResponse(category))
                .thenReturn(expectedResponse);

        CategoryResponse result =
                categoryService.updateStatus(categoryId, request);

        assertSame(expectedResponse, result);

        verify(category).setActive(false);
        verify(categoryRepository).save(category);
        verify(categoryMapper).toResponse(category);
    }

    @Test
    void updateStatus_shouldThrowWhenCategoryDoesNotExist() {
        CategoryStatusUpdateRequest request =
                mock(CategoryStatusUpdateRequest.class);

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.updateStatus(categoryId, request)
        );

        verify(categoryRepository, never())
                .save(any(CategoryEntity.class));
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------

    @Test
    void delete_shouldDeactivateCategoryInsteadOfDeletingIt() {
        CategoryEntity category = mock(CategoryEntity.class);

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        categoryService.delete(categoryId);

        verify(category).setActive(false);
        verify(categoryRepository).save(category);

        verify(categoryRepository, never())
                .deleteById(categoryId);
    }

    @Test
    void delete_shouldThrowWhenCategoryDoesNotExist() {
        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.delete(categoryId)
        );

        verify(categoryRepository, never())
                .save(any(CategoryEntity.class));
    }
}
