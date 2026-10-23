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
import cl.pedidos360.ms_catalog.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Implementación del servicio encargado de gestionar
 * los productos del catálogo.
 *
 * <p>Centraliza las operaciones de creación, consulta,
 * actualización, cambio de precio, gestión de stock,
 * cambio de estado y eliminación lógica.</p>
 *
 * <p>Las operaciones que modifican precios o stock generan
 * registros históricos para mantener la trazabilidad
 * de los cambios realizados.</p>
 *
 * <p>Las operaciones de escritura registran además el
 * usuario autenticado y el identificador de correlación
 * asociado a la solicitud.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final CategoryRepository categoryRepository;

    private final ProductMapper productMapper;
    private final StockMovementMapper stockMovementMapper;
    private final PriceHistoryMapper priceHistoryMapper;

    private final CurrentUserProvider currentUserProvider;
    private final CorrelationIdProvider correlationIdProvider;

    /**
     * Crea un nuevo producto dentro del catálogo.
     *
     * <p>El SKU debe ser único. Cuando el producto posee
     * stock inicial mayor a cero, se genera automáticamente
     * un movimiento de tipo {@link StockMovementType#INITIAL_LOAD}.</p>
     *
     * @param request datos necesarios para crear el producto
     * @return información del producto creado
     * @throws ResourceAlreadyExistsException si el SKU ya existe
     */
    @Transactional
    @Override
    public ProductResponse create(ProductCreateRequest request) {
        UUID userId = currentUserProvider.getCurrentUserId();
        UUID correlationId = correlationIdProvider.getCorrelationId();

        if (productRepository.existsBySku(request.sku())) {
            log.warn(
                    "No se pudo crear el producto porque el SKU ya existe " +
                    "| userId={} | sku={} | correlationId={}",
                    userId,
                    request.sku(),
                    correlationId
            );

            throw new ResourceAlreadyExistsException("Ya existe un producto con el SKU: " + request.sku());
        }

        ProductEntity product = productMapper.toEntity(request);
        UUID categoryId = request.categoryId();

        if (categoryId == null) {
            categoryId = categoryRepository
                    .findByNameIgnoreCase("OTROS")
                    .filter(CategoryEntity::isActive)
                    .map(CategoryEntity::getId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No se encontró una categoría activa por defecto: OTROS"
                    ));
        }

        product.setCategoryId(categoryId);

        ProductEntity savedProduct = productRepository.save(product);

        if (savedProduct.getStock() > 0) {
            StockMovementEntity movement = stockMovementMapper.toEntity(
                    savedProduct.getId(),
                    StockMovementType.INITIAL_LOAD,
                    savedProduct.getStock(),
                    0,
                    savedProduct.getStock(),
                    "Carga inicial de stock.",
                    correlationId
            );

            stockMovementRepository.save(movement);
        }

        log.info(
                "Producto creado correctamente " +
                "| productId={} | sku={} | userId={} | correlationId={}",
                savedProduct.getId(),
                savedProduct.getSku(),
                userId,
                correlationId
        );

        return productMapper.toResponse(savedProduct);
    }

    /**
     * Obtiene un producto por su identificador.
     *
     * @param id identificador único del producto
     * @return información del producto solicitado
     * @throws ResourceNotFoundException si el producto no existe
     */
    @Override
    public ProductResponse findById(UUID id) {
        UUID correlationId = correlationIdProvider.getCorrelationId();

        log.debug(
                "Consultando producto " +
                "| productId={} | correlationId={}",
                id,
                correlationId
        );

        ProductEntity product = findProductById(id);

        return productMapper.toResponse(product);
    }

    /**
     * Busca productos aplicando filtros opcionales y paginación.
     *
     * <p>La búsqueda permite filtrar por nombre, categoría
     * y estado del producto.</p>
     *
     * @param name nombre o parte del nombre del producto
     * @param categoryId identificador de la categoría
     * @param status estado del producto
     * @param pageable configuración de paginación
     * @return página de productos encontrados
     */
    @Override
    public Page<ProductResponse> search(String name, UUID categoryId, ProductStatus status, Pageable pageable) {
        UUID correlationId = correlationIdProvider.getCorrelationId();

        log.debug(
                "Buscando productos " +
                "| name={} | categoryId={} | status={} " +
                "| page={} | size={} | correlationId={}",
                name,
                categoryId,
                status,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                correlationId
        );

        return productRepository
                .search(
                        name,
                        categoryId,
                        status,
                        pageable
                )
                .map(productMapper::toResponse);
    }

    /**
     * Actualiza los datos generales de un producto.
     *
     * <p>La actualización delega al mapper la modificación
     * de los campos permitidos por {@link ProductUpdateRequest}.
     * Los campos controlados por operaciones específicas,
     * como precio y stock, no son modificados aquí.</p>
     *
     * @param id identificador único del producto
     * @param request datos a actualizar
     * @return información actualizada del producto
     * @throws ResourceNotFoundException si el producto no existe
     */
    @Override
    public ProductResponse update(UUID id, ProductUpdateRequest request) {
        UUID correlationId = correlationIdProvider.getCorrelationId();

        ProductEntity product = findProductById(id);

        productMapper.updateEntity(
                product,
                request
        );

        ProductEntity updatedProduct = productRepository.save(product);

        log.info(
                "Producto actualizado correctamente " +
                "| productId={} | userId={} | correlationId={}",
                id,
                currentUserProvider.getCurrentUserId(),
                correlationId
        );

        return productMapper.toResponse(updatedProduct);
    }

    /**
     * Actualiza el estado de un producto.
     *
     * @param id identificador único del producto
     * @param request nuevo estado del producto
     * @return información actualizada del producto
     * @throws ResourceNotFoundException si el producto no existe
     */
    @Override
    public ProductResponse updateStatus(UUID id, ProductStatusUpdateRequest request) {
        UUID correlationId = correlationIdProvider.getCorrelationId();

        ProductEntity product = findProductById(id);
        ProductStatus previousStatus = product.getStatus();

        product.setStatus(request.status());

        ProductEntity updatedProduct = productRepository.save(product);

        log.info(
                "Estado de producto actualizado " +
                "| productId={} | previousStatus={} | newStatus={} " +
                "| userId={} | correlationId={}",
                id,
                previousStatus,
                updatedProduct.getStatus(),
                currentUserProvider.getCurrentUserId(),
                correlationId
        );

        return productMapper.toResponse(updatedProduct);
    }

    /**
     * Actualiza el precio de un producto y registra
     * el cambio en el historial de precios.
     *
     * <p>No se permite establecer el mismo precio que
     * el actualmente registrado.</p>
     *
     * @param id identificador único del producto
     * @param request nuevo precio y motivo del cambio
     * @return información actualizada del producto
     * @throws ResourceNotFoundException si el producto no existe
     * @throws InvalidOperationException si el precio no cambia
     */
    @Transactional
    @Override
    public ProductResponse updatePrice(UUID id, ProductPriceUpdateRequest request) {
        UUID correlationId = correlationIdProvider.getCorrelationId();

        ProductEntity product = findProductById(id);

        BigDecimal previousPrice = product.getPrice();
        BigDecimal newPrice = request.price();

        if (previousPrice.compareTo(newPrice) == 0) {
            throw new InvalidOperationException("El nuevo precio debe ser diferente al precio actual.");
        }

        product.setPrice(newPrice);

        ProductEntity updatedProduct = productRepository.save(product);
        PriceHistoryEntity priceHistory = priceHistoryMapper.toEntity(
                updatedProduct.getId(),
                previousPrice,
                newPrice,
                request.reason(),
                correlationId
        );

        priceHistoryRepository.save(priceHistory);

        log.info(
                "Precio de producto actualizado " +
                "| productId={} | userId={} | correlationId={}",
                id,
                currentUserProvider.getCurrentUserId(),
                correlationId
        );

        return productMapper.toResponse(updatedProduct);
    }

    /**
     * Ajusta el stock de un producto mediante una operación
     * de incremento o disminución.
     *
     * <p>Las disminuciones no pueden superar el stock disponible.
     * Cada ajuste genera un movimiento de tipo
     * {@link StockMovementType#ADJUSTMENT}.</p>
     *
     * @param id identificador único del producto
     * @param request operación, cantidad y motivo del ajuste
     * @return información actualizada del producto
     * @throws ResourceNotFoundException si el producto no existe
     * @throws InsufficientStockException si no existe stock suficiente
     */
    @Transactional
    @Override
    public ProductResponse updateStock(UUID id, ProductStockUpdateRequest request) {
        UUID correlationId = correlationIdProvider.getCorrelationId();

        ProductEntity product = findProductById(id);
        int previousStock = product.getStock();
        int resultingStock;

        if (request.operation() == StockAdjustmentOperation.INCREASE) {
            resultingStock = previousStock + request.quantity();
        } else {
            if (request.quantity() > previousStock) {
                throw new InsufficientStockException("El stock disponible es insuficiente para realizar el ajuste.");
            }

            resultingStock = previousStock - request.quantity();
        }

        product.setStock(resultingStock);

        ProductEntity updatedProduct = productRepository.save(product);

        StockMovementEntity movement =
                stockMovementMapper.toEntity(
                        updatedProduct.getId(),
                        StockMovementType.ADJUSTMENT,
                        request.quantity(),
                        previousStock,
                        resultingStock,
                        request.reason(),
                        correlationId
                );

        stockMovementRepository.save(movement);

        log.info(
                "Stock de producto ajustado " +
                "| productId={} | operation={} | quantity={} " +
                "| previousStock={} | resultingStock={} " +
                "| userId={} | correlationId={}",
                id,
                request.operation(),
                request.quantity(),
                previousStock,
                resultingStock,
                currentUserProvider.getCurrentUserId(),
                correlationId
        );

        return productMapper.toResponse(updatedProduct);
    }

    /**
     * Repone stock de un producto.
     *
     * <p>La cantidad indicada se suma al stock actual
     * y se registra un movimiento de tipo
     * {@link StockMovementType#RESTOCK}.</p>
     *
     * @param id identificador único del producto
     * @param request cantidad y motivo de la reposición
     * @return información actualizada del producto
     * @throws ResourceNotFoundException si el producto no existe
     */
    @Transactional
    @Override
    public ProductResponse restock(UUID id, ProductRestockRequest request) {
        UUID correlationId = correlationIdProvider.getCorrelationId();

        ProductEntity product = findProductById(id);

        int previousStock = product.getStock();
        int resultingStock = previousStock + request.quantity();

        product.setStock(resultingStock);

        ProductEntity updatedProduct = productRepository.save(product);

        StockMovementEntity movement =
                stockMovementMapper.toEntity(
                        updatedProduct.getId(),
                        StockMovementType.RESTOCK,
                        request.quantity(),
                        previousStock,
                        resultingStock,
                        request.reason(),
                        correlationId
                );

        stockMovementRepository.save(movement);

        log.info(
                "Stock de producto repuesto " +
                "| productId={} | quantity={} " +
                "| previousStock={} | resultingStock={} " +
                "| userId={} | correlationId={}",
                id,
                request.quantity(),
                previousStock,
                resultingStock,
                currentUserProvider.getCurrentUserId(),
                correlationId
        );

        return productMapper.toResponse(updatedProduct);
    }

    /**
     * Realiza la eliminación lógica de un producto.
     *
     * <p>El producto no se elimina físicamente de MongoDB.
     * Su estado se cambia a {@link ProductStatus#INACTIVE}
     * para conservar su información histórica.</p>
     *
     * @param id identificador único del producto
     * @throws ResourceNotFoundException si el producto no existe
     */
    @Override
    public void delete(UUID id) {
        UUID correlationId = correlationIdProvider.getCorrelationId();

        ProductEntity product = findProductById(id);

        product.setStatus(ProductStatus.INACTIVE);

        productRepository.save(product);

        log.info(
                "Producto eliminado lógicamente " +
                "| productId={} | userId={} | correlationId={}",
                id,
                currentUserProvider.getCurrentUserId(),
                correlationId
        );
    }

    /**
     * Ejecuta el descuento de stock para varios productos y registra
     * un movimiento de inventario por cada producto procesado.
     *
     * @param request productos y cantidades que se deben descontar
     * @return productos actualizados después de aplicar los descuentos
     * @throws IllegalArgumentException si la solicitud está vacía,
     *         contiene datos inválidos o productos duplicados
     * @throws InsufficientStockException si el stock disponible
     *         es insuficiente para alguno de los productos
     * @throws ResourceNotFoundException si alguno de los productos
     *         solicitados no existe
     */
    @Override
    @Transactional
    public List<ProductResponse> bulkReduceStock(
            List<ProductStockReductionItemRequest> request
    ) {
        UUID correlationId = correlationIdProvider.getCorrelationId();

        log.info(
                "Iniciando descuento masivo de stock. CorrelationId: {}, cantidad de productos: {}",
                correlationId,
                request == null ? 0 : request.size()
        );

        if (request == null || request.isEmpty()) {
            log.warn(
                    "Descuento masivo rechazado. CorrelationId: {}. Motivo: lista vacía o nula.",
                    correlationId
            );

            throw new IllegalArgumentException(
                    "Debe indicar al menos un producto."
            );
        }

        Set<UUID> productIds = new HashSet<>();

        for (ProductStockReductionItemRequest item : request) {
            if (item == null
                    || item.productId() == null
                    || item.quantity() == null
                    || item.quantity() <= 0) {

                log.warn(
                        "Descuento masivo rechazado. CorrelationId: {}. Motivo: elemento inválido.",
                        correlationId
                );

                throw new IllegalArgumentException(
                        "Cada producto debe tener un ID válido y una cantidad mayor que cero."
                );
            }

            if (!productIds.add(item.productId())) {
                log.warn(
                        "Descuento masivo rechazado. CorrelationId: {}, producto duplicado: {}",
                        correlationId,
                        item.productId()
                );

                throw new IllegalArgumentException(
                        "La solicitud contiene productos duplicados: "
                                + item.productId()
                );
            }
        }

        log.info(
                "Validaciones iniciales completadas. CorrelationId: {}, productos: {}",
                correlationId,
                request.size()
        );

        List<ProductEntity> products = new ArrayList<>();

        for (ProductStockReductionItemRequest item : request) {
            ProductEntity product = findProductById(item.productId());

            if (product.getStock() == null
                    || product.getStock() < item.quantity()) {

                log.warn(
                        "Descuento masivo rechazado por stock insuficiente. " +
                                "CorrelationId: {}, ProductId: {}, stock disponible: {}, cantidad solicitada: {}",
                        correlationId,
                        item.productId(),
                        product.getStock(),
                        item.quantity()
                );

                throw new InsufficientStockException(
                        "Stock insuficiente para el producto: "
                                + item.productId()
                );
            }

            products.add(product);
        }

        log.info(
                "Existencia y stock verificados. CorrelationId: {}. Iniciando actualización.",
                correlationId
        );

        List<ProductResponse> responses = new ArrayList<>();

        for (int i = 0; i < request.size(); i++) {
            ProductStockReductionItemRequest item = request.get(i);
            ProductEntity product = products.get(i);

            Integer previousStock = product.getStock();
            Integer resultingStock = previousStock - item.quantity();

            product.setStock(resultingStock);

            ProductEntity savedProduct = productRepository.save(product);

            StockMovementEntity movement = stockMovementMapper.toEntity(
                    savedProduct.getId(),
                    StockMovementType.ADJUSTMENT,
                    item.quantity(),
                    previousStock,
                    resultingStock,
                    "Descuento de stock masivo",
                    correlationId
            );

            stockMovementRepository.save(movement);

            responses.add(productMapper.toResponse(savedProduct));

            log.info(
                    "Stock actualizado. CorrelationId: {}, ProductId: {}, " +
                            "stock anterior: {}, cantidad descontada: {}, stock resultante: {}",
                    correlationId,
                    savedProduct.getId(),
                    previousStock,
                    item.quantity(),
                    resultingStock
            );
        }

        log.info(
                "Descuento masivo de stock completado. CorrelationId: {}, productos procesados: {}",
                correlationId,
                responses.size()
        );

        return responses;
    }

    /**
     * Busca un producto por su identificador.
     *
     * @param id identificador único del producto
     * @return entidad encontrada
     * @throws ResourceNotFoundException si el producto no existe
     */
    private ProductEntity findProductById(UUID id) {
        return productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un producto con el ID: " + id
                ));
    }
}
