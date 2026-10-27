package cl.pedidos360.ms_catalog.data;

import cl.pedidos360.ms_catalog.entity.CategoryEntity;
import cl.pedidos360.ms_catalog.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryDataInitializerTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryDataInitializer categoryDataInitializer;

    @Test
    void run_shouldCreateDefaultCategory_whenItDoesNotExist()
            throws Exception {

        when(categoryRepository.existsByNameIgnoreCase("OTROS"))
                .thenReturn(false);

        categoryDataInitializer.run();

        ArgumentCaptor<CategoryEntity> captor =
                ArgumentCaptor.forClass(CategoryEntity.class);

        verify(categoryRepository).save(captor.capture());

        CategoryEntity savedCategory = captor.getValue();

        assertEquals("OTROS", savedCategory.getName());
        assertEquals(
                "Productos sin categoria definida.",
                savedCategory.getDescription()
        );
        assertTrue(savedCategory.isActive());
    }

    @Test
    void run_shouldNotCreateDefaultCategory_whenItAlreadyExists()
            throws Exception {

        when(categoryRepository.existsByNameIgnoreCase("OTROS"))
                .thenReturn(true);

        categoryDataInitializer.run();

        verify(categoryRepository)
                .existsByNameIgnoreCase("OTROS");

        verify(categoryRepository, never())
                .save(org.mockito.ArgumentMatchers.any(CategoryEntity.class));
    }
}
