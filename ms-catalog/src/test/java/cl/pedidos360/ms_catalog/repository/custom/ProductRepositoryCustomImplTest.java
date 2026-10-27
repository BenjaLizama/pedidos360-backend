package cl.pedidos360.ms_catalog.repository.custom;

import cl.pedidos360.ms_catalog.entity.ProductEntity;
import cl.pedidos360.ms_catalog.enums.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ProductRepositoryCustomImplTest {

    private MongoTemplate mongoTemplate;
    private ProductRepositoryCustomImpl productRepository;

    @BeforeEach
    void setUp() {
        mongoTemplate = mock(MongoTemplate.class);
        productRepository = new ProductRepositoryCustomImpl(mongoTemplate);
    }

    @Test
    void shouldSearchWithoutFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        List<ProductEntity> products = List.of(
                mock(ProductEntity.class),
                mock(ProductEntity.class)
        );

        when(mongoTemplate.count(any(Query.class), eq(ProductEntity.class)))
                .thenReturn(2L);

        when(mongoTemplate.find(any(Query.class), eq(ProductEntity.class)))
                .thenReturn(products);

        Page<ProductEntity> result = productRepository.search(
                null,
                null,
                null,
                pageable
        );

        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(products, result.getContent());

        verify(mongoTemplate).count(
                argThat(query -> query.getQueryObject().isEmpty()),
                eq(ProductEntity.class)
        );

        verify(mongoTemplate).find(
                argThat(query -> query.getQueryObject().isEmpty()),
                eq(ProductEntity.class)
        );
    }

    @Test
    void shouldIgnoreBlankNameAndSearchWithoutFilters() {
        Pageable pageable = PageRequest.of(0, 10);

        when(mongoTemplate.count(any(Query.class), eq(ProductEntity.class)))
                .thenReturn(0L);

        when(mongoTemplate.find(any(Query.class), eq(ProductEntity.class)))
                .thenReturn(List.of());

        Page<ProductEntity> result = productRepository.search(
                "   ",
                null,
                null,
                pageable
        );

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(mongoTemplate).count(
                argThat(query -> query.getQueryObject().isEmpty()),
                eq(ProductEntity.class)
        );
    }

    @Test
    void shouldApplyNameFilterIgnoringCaseAndEscapingSpecialCharacters() {
        Pageable pageable = PageRequest.of(0, 10);
        String name = "  Mouse.+Gaming  ";

        when(mongoTemplate.count(any(Query.class), eq(ProductEntity.class)))
                .thenReturn(1L);

        when(mongoTemplate.find(any(Query.class), eq(ProductEntity.class)))
                .thenReturn(List.of(mock(ProductEntity.class)));

        Page<ProductEntity> result = productRepository.search(
                name,
                null,
                null,
                pageable
        );

        assertEquals(1, result.getTotalElements());

        verify(mongoTemplate).count(
                argThat(query -> {
                    String queryText = query.getQueryObject().toString();

                    return queryText.contains("name")
                            && queryText.contains("Mouse.+Gaming")
                            && queryText.contains("i");
                }),
                eq(ProductEntity.class)
        );
    }

    @Test
    void shouldApplyCategoryFilter() {
        UUID categoryId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(mongoTemplate.count(any(Query.class), eq(ProductEntity.class)))
                .thenReturn(1L);

        when(mongoTemplate.find(any(Query.class), eq(ProductEntity.class)))
                .thenReturn(List.of(mock(ProductEntity.class)));

        Page<ProductEntity> result = productRepository.search(
                null,
                categoryId,
                null,
                pageable
        );

        assertEquals(1, result.getTotalElements());

        verify(mongoTemplate).count(
                argThat(query ->
                        query.getQueryObject().toString().contains("categoryId")
                                && query.getQueryObject().toString()
                                .contains(categoryId.toString())
                ),
                eq(ProductEntity.class)
        );
    }

    @Test
    void shouldApplyStatusFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        ProductStatus status = ProductStatus.ACTIVE;

        when(mongoTemplate.count(any(Query.class), eq(ProductEntity.class)))
                .thenReturn(1L);

        when(mongoTemplate.find(any(Query.class), eq(ProductEntity.class)))
                .thenReturn(List.of(mock(ProductEntity.class)));

        Page<ProductEntity> result = productRepository.search(
                null,
                null,
                status,
                pageable
        );

        assertEquals(1, result.getTotalElements());

        verify(mongoTemplate).count(
                argThat(query ->
                        query.getQueryObject().toString().contains("status")
                                && query.getQueryObject().toString()
                                .contains(status.name())
                ),
                eq(ProductEntity.class)
        );
    }

    @Test
    void shouldCombineAllFiltersAndApplyPagination() {
        String name = "Keyboard";
        UUID categoryId = UUID.randomUUID();
        ProductStatus status = ProductStatus.ACTIVE;
        Pageable pageable = PageRequest.of(2, 5);

        List<ProductEntity> products = List.of(
                mock(ProductEntity.class),
                mock(ProductEntity.class)
        );

        when(mongoTemplate.count(any(Query.class), eq(ProductEntity.class)))
                .thenReturn(12L);

        when(mongoTemplate.find(any(Query.class), eq(ProductEntity.class)))
                .thenReturn(products);

        Page<ProductEntity> result = productRepository.search(
                name,
                categoryId,
                status,
                pageable
        );

        assertEquals(12, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        assertEquals(2, result.getNumber());
        assertEquals(5, result.getSize());
        assertEquals(products, result.getContent());

        verify(mongoTemplate).count(
                argThat(query -> {
                    String queryText = query.getQueryObject().toString();

                    return queryText.contains("name")
                            && queryText.contains(name)
                            && queryText.contains("categoryId")
                            && queryText.contains(categoryId.toString())
                            && queryText.contains("status")
                            && queryText.contains(status.name());
                }),
                eq(ProductEntity.class)
        );

        verify(mongoTemplate).find(
                argThat(query ->
                        query.getSkip() == 10
                                && query.getLimit() == 5
                                && query.getQueryObject().toString()
                                .contains("categoryId")
                ),
                eq(ProductEntity.class)
        );
    }

    @Test
    void shouldReturnEmptyPageWhenNoProductsMatch() {
        Pageable pageable = PageRequest.of(0, 10);

        when(mongoTemplate.count(any(Query.class), eq(ProductEntity.class)))
                .thenReturn(0L);

        when(mongoTemplate.find(any(Query.class), eq(ProductEntity.class)))
                .thenReturn(List.of());

        Page<ProductEntity> result = productRepository.search(
                "NonexistentProduct",
                null,
                null,
                pageable
        );

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());

        verify(mongoTemplate).count(
                any(Query.class),
                eq(ProductEntity.class)
        );

        verify(mongoTemplate).find(
                any(Query.class),
                eq(ProductEntity.class)
        );
    }
}
