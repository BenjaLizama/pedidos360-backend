package cl.pedidos360.ms_catalog.service.impl;

import cl.pedidos360.ms_catalog.dto.request.*;
import cl.pedidos360.ms_catalog.dto.response.ProductResponse;
import cl.pedidos360.ms_catalog.entity.CategoryEntity;
import cl.pedidos360.ms_catalog.entity.PriceHistoryEntity;
import cl.pedidos360.ms_catalog.entity.ProductEntity;
import cl.pedidos360.ms_catalog.entity.StockMovementEntity;
import cl.pedidos360.ms_catalog.enums.ProductStatus;
import cl.pedidos360.ms_catalog.enums.StockAdjustmentOperation;
import cl.pedidos360.ms_catalog.enums.StockMovementType;
import cl.pedidos360.ms_catalog.exception.InsufficientStockException;
import cl.pedidos360.ms_catalog.exception.InvalidOperationException;
import cl.pedidos360.ms_catalog.exception.ResourceAlreadyExistsException;
import cl.pedidos360.ms_catalog.exception.ResourceNotFoundException;
import cl.pedidos360.ms_catalog.mapper.PriceHistoryMapper;
import cl.pedidos360.ms_catalog.mapper.ProductMapper;
import cl.pedidos360.ms_catalog.mapper.StockMovementMapper;
import cl.pedidos360.ms_catalog.observability.CorrelationIdProvider;
import cl.pedidos360.ms_catalog.repository.CategoryRepository;
import cl.pedidos360.ms_catalog.repository.PriceHistoryRepository;
import cl.pedidos360.ms_catalog.repository.ProductRepository;
import cl.pedidos360.ms_catalog.repository.StockMovementRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private StockMovementMapper stockMovementMapper;

    @Mock
    private PriceHistoryMapper priceHistoryMapper;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private CorrelationIdProvider correlationIdProvider;

    @InjectMocks
    private ProductServiceImpl productService;

    private final UUID productId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID correlationId = UUID.randomUUID();

    private ProductEntity product;
    private ProductResponse response;

    @BeforeEach
    void setUp() {
        product = mock(ProductEntity.class);
        response = mock(ProductResponse.class);

        lenient()
                .when(correlationIdProvider.getCorrelationId())
                .thenReturn(correlationId);

        lenient()
                .when(currentUserProvider.getCurrentUserId())
                .thenReturn(userId);

        lenient()
                .when(productRepository.save(any(ProductEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        lenient()
                .when(productMapper.toResponse(any(ProductEntity.class)))
                .thenReturn(response);
    }

    /**
     * Configura el mapper de movimientos para que devuelva
     * una entidad válida cuando el servicio registra stock.
     */
    private void mockStockMovementMapper() {
        when(stockMovementMapper.toEntity(
                any(UUID.class),
                any(StockMovementType.class),
                anyInt(),
                anyInt(),
                anyInt(),
                nullable(String.class),
                any(UUID.class)
        )).thenReturn(mock(StockMovementEntity.class));
    }

    /**
     * Configura el mapper del historial de precios para que
     * devuelva una entidad válida al cambiar el precio.
     */
    private void mockPriceHistoryMapper() {
        when(priceHistoryMapper.toEntity(
                any(UUID.class),
                any(BigDecimal.class),
                any(BigDecimal.class),
                nullable(String.class),
                any(UUID.class)
        )).thenReturn(mock(PriceHistoryEntity.class));
    }

    // ---------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------

    @Test
    void create_shouldCreateProductAndInitialStockMovement() {
        ProductCreateRequest request = new ProductCreateRequest(
                "SKU-001",
                "Teclado mecánico",
                "Teclado para gaming",
                new BigDecimal("49990"),
                10,
                categoryId
        );

        mockStockMovementMapper();

        when(productRepository.existsBySku("SKU-001"))
                .thenReturn(false);

        when(productMapper.toEntity(request))
                .thenReturn(product);

        when(product.getStock()).thenReturn(10);
        when(product.getId()).thenReturn(productId);

        ProductResponse result = productService.create(request);

        assertSame(response, result);

        verify(product).setCategoryId(categoryId);
        verify(productRepository).save(product);

        verify(stockMovementMapper).toEntity(
                productId,
                StockMovementType.INITIAL_LOAD,
                10,
                0,
                10,
                "Carga inicial de stock.",
                correlationId
        );

        verify(stockMovementRepository)
                .save(any(StockMovementEntity.class));

        verify(productMapper).toResponse(product);
    }

    @Test
    void create_shouldNotRegisterInitialMovementWhenStockIsZero() {
        ProductCreateRequest request = new ProductCreateRequest(
                "SKU-002",
                "Mouse",
                "Mouse óptico",
                new BigDecimal("12990"),
                0,
                categoryId
        );

        when(productRepository.existsBySku("SKU-002"))
                .thenReturn(false);

        when(productMapper.toEntity(request))
                .thenReturn(product);

        when(product.getStock()).thenReturn(0);

        ProductResponse result = productService.create(request);

        assertSame(response, result);

        verify(productRepository).save(product);
        verifyNoInteractions(stockMovementMapper);
        verifyNoInteractions(stockMovementRepository);
    }

    @Test
    void create_shouldAssignDefaultCategoryWhenCategoryIsNull() {
        ProductCreateRequest request = new ProductCreateRequest(
                "SKU-003",
                "Monitor",
                "Monitor de 24 pulgadas",
                new BigDecimal("149990"),
                0,
                null
        );

        CategoryEntity defaultCategory = mock(CategoryEntity.class);

        when(productRepository.existsBySku("SKU-003"))
                .thenReturn(false);

        when(productMapper.toEntity(request))
                .thenReturn(product);

        when(categoryRepository.findByNameIgnoreCase("OTROS"))
                .thenReturn(Optional.of(defaultCategory));

        when(defaultCategory.isActive()).thenReturn(true);
        when(defaultCategory.getId()).thenReturn(categoryId);
        when(product.getStock()).thenReturn(0);

        ProductResponse result = productService.create(request);

        assertSame(response, result);

        verify(product).setCategoryId(categoryId);
        verify(categoryRepository).findByNameIgnoreCase("OTROS");
        verify(productRepository).save(product);
        verifyNoInteractions(stockMovementRepository);
    }

    @Test
    void create_shouldThrowWhenSkuAlreadyExists() {
        ProductCreateRequest request = new ProductCreateRequest(
                "SKU-001",
                "Teclado",
                "Descripción",
                new BigDecimal("49990"),
                10,
                categoryId
        );

        when(productRepository.existsBySku("SKU-001"))
                .thenReturn(true);

        assertThrows(
                ResourceAlreadyExistsException.class,
                () -> productService.create(request)
        );

        verify(productRepository, never())
                .save(any(ProductEntity.class));

        verifyNoInteractions(productMapper);
        verifyNoInteractions(stockMovementMapper);
        verifyNoInteractions(stockMovementRepository);
    }

    @Test
    void create_shouldThrowWhenDefaultCategoryDoesNotExist() {
        ProductCreateRequest request = new ProductCreateRequest(
                "SKU-004",
                "Audífonos",
                "Audífonos inalámbricos",
                new BigDecimal("29990"),
                0,
                null
        );

        when(productRepository.existsBySku("SKU-004"))
                .thenReturn(false);

        when(productMapper.toEntity(request))
                .thenReturn(product);

        when(categoryRepository.findByNameIgnoreCase("OTROS"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.create(request)
        );

        verify(productRepository, never())
                .save(any(ProductEntity.class));

        verifyNoInteractions(stockMovementMapper);
        verifyNoInteractions(stockMovementRepository);
    }

    @Test
    void create_shouldThrowWhenDefaultCategoryIsInactive() {
        ProductCreateRequest request = new ProductCreateRequest(
                "SKU-005",
                "Parlante",
                "Parlante Bluetooth",
                new BigDecimal("39990"),
                0,
                null
        );

        CategoryEntity inactiveCategory = mock(CategoryEntity.class);

        when(productRepository.existsBySku("SKU-005"))
                .thenReturn(false);

        when(productMapper.toEntity(request))
                .thenReturn(product);

        when(categoryRepository.findByNameIgnoreCase("OTROS"))
                .thenReturn(Optional.of(inactiveCategory));

        when(inactiveCategory.isActive()).thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.create(request)
        );

        verify(productRepository, never())
                .save(any(ProductEntity.class));

        verifyNoInteractions(stockMovementMapper);
        verifyNoInteractions(stockMovementRepository);
    }

    // ---------------------------------------------------------
    // FIND BY ID
    // ---------------------------------------------------------

    @Test
    void findById_shouldReturnProductWhenItExists() {
        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        ProductResponse result = productService.findById(productId);

        assertSame(response, result);
        verify(productMapper).toResponse(product);
    }

    @Test
    void findById_shouldThrowWhenProductDoesNotExist() {
        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.findById(productId)
        );

        verify(productMapper, never())
                .toResponse(any(ProductEntity.class));
    }

    // ---------------------------------------------------------
    // SEARCH
    // ---------------------------------------------------------

    @Test
    void search_shouldReturnMappedPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<ProductEntity> productPage =
                new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.search(
                "teclado",
                categoryId,
                ProductStatus.ACTIVE,
                pageable
        )).thenReturn(productPage);

        Page<ProductResponse> result = productService.search(
                "teclado",
                categoryId,
                ProductStatus.ACTIVE,
                pageable
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertSame(response, result.getContent().get(0));

        verify(productRepository).search(
                "teclado",
                categoryId,
                ProductStatus.ACTIVE,
                pageable
        );

        verify(productMapper).toResponse(product);
    }

    @Test
    void search_shouldReturnEmptyPageWhenThereAreNoResults() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<ProductEntity> emptyPage = Page.empty(pageable);

        when(productRepository.search(
                isNull(),
                isNull(),
                isNull(),
                eq(pageable)
        )).thenReturn(emptyPage);

        Page<ProductResponse> result = productService.search(
                null,
                null,
                null,
                pageable
        );

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(productRepository).search(
                null,
                null,
                null,
                pageable
        );

        verifyNoInteractions(productMapper);
    }

    // ---------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------

    @Test
    void update_shouldUpdateAndReturnProduct() {
        ProductUpdateRequest request = mock(ProductUpdateRequest.class);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        ProductResponse result = productService.update(productId, request);

        assertSame(response, result);

        verify(productMapper).updateEntity(product, request);
        verify(productRepository).save(product);
        verify(productMapper).toResponse(product);
    }

    @Test
    void update_shouldThrowWhenProductDoesNotExist() {
        ProductUpdateRequest request = mock(ProductUpdateRequest.class);

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.update(productId, request)
        );

        verify(productRepository, never())
                .save(any(ProductEntity.class));

        verify(productMapper, never()).updateEntity(
                any(ProductEntity.class),
                any(ProductUpdateRequest.class)
        );
    }

    // ---------------------------------------------------------
    // UPDATE STATUS
    // ---------------------------------------------------------

    @Test
    void updateStatus_shouldChangeProductStatus() {
        ProductStatusUpdateRequest request =
                mock(ProductStatusUpdateRequest.class);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(request.status()).thenReturn(ProductStatus.ACTIVE);

        ProductResponse result =
                productService.updateStatus(productId, request);

        assertSame(response, result);

        verify(product).setStatus(ProductStatus.ACTIVE);
        verify(productRepository).save(product);
        verify(productMapper).toResponse(product);
    }

    @Test
    void updateStatus_shouldThrowWhenProductDoesNotExist() {
        ProductStatusUpdateRequest request =
                mock(ProductStatusUpdateRequest.class);

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateStatus(productId, request)
        );

        verify(productRepository, never())
                .save(any(ProductEntity.class));

        verify(product, never()).setStatus(any(ProductStatus.class));
    }

    // ---------------------------------------------------------
    // UPDATE PRICE
    // ---------------------------------------------------------

    @Test
    void updatePrice_shouldSaveProductAndPriceHistory() {
        ProductPriceUpdateRequest request =
                mock(ProductPriceUpdateRequest.class);

        BigDecimal previousPrice = new BigDecimal("10000");
        BigDecimal newPrice = new BigDecimal("12000");

        mockPriceHistoryMapper();

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(product.getPrice()).thenReturn(previousPrice);
        when(product.getId()).thenReturn(productId);
        when(request.price()).thenReturn(newPrice);
        when(request.reason()).thenReturn("Ajuste de precio");

        ProductResponse result =
                productService.updatePrice(productId, request);

        assertSame(response, result);

        verify(product).setPrice(newPrice);
        verify(productRepository).save(product);

        verify(priceHistoryMapper).toEntity(
                productId,
                previousPrice,
                newPrice,
                "Ajuste de precio",
                correlationId
        );

        verify(priceHistoryRepository)
                .save(any(PriceHistoryEntity.class));

        verify(productMapper).toResponse(product);
    }

    @Test
    void updatePrice_shouldAllowChangeToLowerPrice() {
        ProductPriceUpdateRequest request =
                mock(ProductPriceUpdateRequest.class);

        BigDecimal previousPrice = new BigDecimal("20000");
        BigDecimal newPrice = new BigDecimal("15000");

        mockPriceHistoryMapper();

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(product.getPrice()).thenReturn(previousPrice);
        when(product.getId()).thenReturn(productId);
        when(request.price()).thenReturn(newPrice);
        when(request.reason()).thenReturn("Descuento");

        ProductResponse result =
                productService.updatePrice(productId, request);

        assertSame(response, result);

        verify(product).setPrice(newPrice);
        verify(productRepository).save(product);

        verify(priceHistoryMapper).toEntity(
                productId,
                previousPrice,
                newPrice,
                "Descuento",
                correlationId
        );

        verify(priceHistoryRepository)
                .save(any(PriceHistoryEntity.class));
    }

    @Test
    void updatePrice_shouldThrowWhenPriceDoesNotChange() {
        ProductPriceUpdateRequest request =
                mock(ProductPriceUpdateRequest.class);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(product.getPrice()).thenReturn(new BigDecimal("10000"));
        when(request.price()).thenReturn(new BigDecimal("10000.00"));

        assertThrows(
                InvalidOperationException.class,
                () -> productService.updatePrice(productId, request)
        );

        verify(productRepository, never())
                .save(any(ProductEntity.class));

        verifyNoInteractions(priceHistoryMapper);
        verifyNoInteractions(priceHistoryRepository);
    }

    @Test
    void updatePrice_shouldThrowWhenProductDoesNotExist() {
        ProductPriceUpdateRequest request =
                mock(ProductPriceUpdateRequest.class);

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updatePrice(productId, request)
        );

        verify(productRepository, never())
                .save(any(ProductEntity.class));

        verifyNoInteractions(priceHistoryMapper);
        verifyNoInteractions(priceHistoryRepository);
    }

    // ---------------------------------------------------------
    // UPDATE STOCK
    // ---------------------------------------------------------

    @Test
    void updateStock_shouldIncreaseStockAndRegisterMovement() {
        ProductStockUpdateRequest request =
                mock(ProductStockUpdateRequest.class);

        mockStockMovementMapper();

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(product.getStock()).thenReturn(10);
        when(product.getId()).thenReturn(productId);
        when(request.operation())
                .thenReturn(StockAdjustmentOperation.INCREASE);
        when(request.quantity()).thenReturn(5);
        when(request.reason()).thenReturn("Reposición manual");

        ProductResponse result =
                productService.updateStock(productId, request);

        assertSame(response, result);

        verify(product).setStock(15);
        verify(productRepository).save(product);

        verify(stockMovementMapper).toEntity(
                productId,
                StockMovementType.ADJUSTMENT,
                5,
                10,
                15,
                "Reposición manual",
                correlationId
        );

        verify(stockMovementRepository)
                .save(any(StockMovementEntity.class));
    }

    @Test
    void updateStock_shouldDecreaseStockAndRegisterMovement() {
        ProductStockUpdateRequest request =
                mock(ProductStockUpdateRequest.class);

        mockStockMovementMapper();

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(product.getStock()).thenReturn(10);
        when(product.getId()).thenReturn(productId);
        when(request.operation())
                .thenReturn(StockAdjustmentOperation.DECREASE);
        when(request.quantity()).thenReturn(4);
        when(request.reason()).thenReturn("Ajuste de inventario");

        ProductResponse result =
                productService.updateStock(productId, request);

        assertSame(response, result);

        verify(product).setStock(6);
        verify(productRepository).save(product);

        verify(stockMovementMapper).toEntity(
                productId,
                StockMovementType.ADJUSTMENT,
                4,
                10,
                6,
                "Ajuste de inventario",
                correlationId
        );

        verify(stockMovementRepository)
                .save(any(StockMovementEntity.class));
    }

    @Test
    void updateStock_shouldAllowDecreasingStockToZero() {
        ProductStockUpdateRequest request =
                mock(ProductStockUpdateRequest.class);

        mockStockMovementMapper();

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(product.getStock()).thenReturn(5);
        when(product.getId()).thenReturn(productId);
        when(request.operation())
                .thenReturn(StockAdjustmentOperation.DECREASE);
        when(request.quantity()).thenReturn(5);
        when(request.reason()).thenReturn("Agotar stock");

        productService.updateStock(productId, request);

        verify(product).setStock(0);
        verify(productRepository).save(product);

        verify(stockMovementMapper).toEntity(
                productId,
                StockMovementType.ADJUSTMENT,
                5,
                5,
                0,
                "Agotar stock",
                correlationId
        );

        verify(stockMovementRepository)
                .save(any(StockMovementEntity.class));
    }

    @Test
    void updateStock_shouldThrowWhenStockIsInsufficient() {
        ProductStockUpdateRequest request =
                mock(ProductStockUpdateRequest.class);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(product.getStock()).thenReturn(3);
        when(request.operation())
                .thenReturn(StockAdjustmentOperation.DECREASE);
        when(request.quantity()).thenReturn(5);

        assertThrows(
                InsufficientStockException.class,
                () -> productService.updateStock(productId, request)
        );

        verify(productRepository, never())
                .save(any(ProductEntity.class));

        verifyNoInteractions(stockMovementMapper);
        verifyNoInteractions(stockMovementRepository);
    }

    @Test
    void updateStock_shouldThrowWhenProductDoesNotExist() {
        ProductStockUpdateRequest request =
                mock(ProductStockUpdateRequest.class);

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateStock(productId, request)
        );

        verify(productRepository, never())
                .save(any(ProductEntity.class));

        verifyNoInteractions(stockMovementMapper);
        verifyNoInteractions(stockMovementRepository);
    }

    // ---------------------------------------------------------
    // RESTOCK
    // ---------------------------------------------------------

    @Test
    void restock_shouldIncreaseStockAndRegisterMovement() {
        ProductRestockRequest request = mock(ProductRestockRequest.class);

        mockStockMovementMapper();

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(product.getStock()).thenReturn(10);
        when(product.getId()).thenReturn(productId);
        when(request.quantity()).thenReturn(20);
        when(request.reason()).thenReturn("Reposición de proveedor");

        ProductResponse result =
                productService.restock(productId, request);

        assertSame(response, result);

        verify(product).setStock(30);
        verify(productRepository).save(product);

        verify(stockMovementMapper).toEntity(
                productId,
                StockMovementType.RESTOCK,
                20,
                10,
                30,
                "Reposición de proveedor",
                correlationId
        );

        verify(stockMovementRepository)
                .save(any(StockMovementEntity.class));
    }

    @Test
    void restock_shouldThrowWhenProductDoesNotExist() {
        ProductRestockRequest request = mock(ProductRestockRequest.class);

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.restock(productId, request)
        );

        verify(productRepository, never())
                .save(any(ProductEntity.class));

        verifyNoInteractions(stockMovementMapper);
        verifyNoInteractions(stockMovementRepository);
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------

    @Test
    void delete_shouldMarkProductAsInactive() {
        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        productService.delete(productId);

        verify(product).setStatus(ProductStatus.INACTIVE);
        verify(productRepository).save(product);

        verify(productMapper, never())
                .toResponse(any(ProductEntity.class));

        verifyNoInteractions(priceHistoryRepository);
        verifyNoInteractions(stockMovementRepository);
    }

    @Test
    void delete_shouldThrowWhenProductDoesNotExist() {
        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.delete(productId)
        );

        verify(productRepository, never())
                .save(any(ProductEntity.class));

        verify(product, never())
                .setStatus(any(ProductStatus.class));
    }
}
