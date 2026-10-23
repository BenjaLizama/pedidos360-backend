package cl.pedidos360.ms_catalog.service;

import cl.pedidos360.ms_catalog.dto.request.*;
import cl.pedidos360.ms_catalog.dto.response.ProductResponse;
import cl.pedidos360.ms_catalog.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Servicio principal encargado de gestionar la lógica de negocio
 * relacionada con los productos del catálogo.
 *
 * <p>Centraliza las operaciones de creación, consulta, actualización
 * y eliminación lógica de productos, además de las operaciones
 * específicas relacionadas con precio e inventario.</p>
 */
public interface ProductService {

    /**
     * Crea un nuevo producto en el catálogo.
     *
     * @param request datos necesarios para crear el producto
     * @return información del producto creado
     */
    ProductResponse create(ProductCreateRequest request);

    /**
     * Obtiene un producto mediante su identificador.
     *
     * @param id identificador único del producto
     * @return información del producto encontrado
     */
    ProductResponse findById(UUID id);

    /**
     * Busca productos aplicando filtros opcionales y paginación.
     *
     * @param name nombre o texto utilizado para filtrar productos
     * @param categoryId identificador de la categoría utilizada como filtro
     * @param status estado del producto utilizado como filtro
     * @param pageable configuración de paginación y ordenamiento
     * @return página de productos que cumplen los filtros
     */
    Page<ProductResponse> search(
            String name,
            UUID categoryId,
            ProductStatus status,
            Pageable pageable
    );

    /**
     * Actualiza la información descriptiva de un producto.
     *
     * @param id identificador único del producto
     * @param request nuevos datos descriptivos del producto
     * @return información actualizada del producto
     */
    ProductResponse update(
            UUID id,
            ProductUpdateRequest request
    );

    /**
     * Actualiza exclusivamente el estado de un producto.
     *
     * @param id identificador único del producto
     * @param request nuevo estado del producto
     * @return información actualizada del producto
     */
    ProductResponse updateStatus(
            UUID id,
            ProductStatusUpdateRequest request
    );

    /**
     * Actualiza el precio de un producto y registra
     * el cambio correspondiente en su historial.
     *
     * @param id identificador único del producto
     * @param request nuevo precio y motivo del cambio
     * @return información actualizada del producto
     */
    ProductResponse updatePrice(
            UUID id,
            ProductPriceUpdateRequest request
    );

    /**
     * Realiza un ajuste manual sobre el inventario de un producto.
     *
     * @param id identificador único del producto
     * @param request datos del ajuste de inventario
     * @return información actualizada del producto
     */
    ProductResponse updateStock(
            UUID id,
            ProductStockUpdateRequest request
    );

    /**
     * Incrementa el stock disponible mediante una operación
     * de reposición.
     *
     * @param id identificador único del producto
     * @param request datos de la reposición
     * @return información actualizada del producto
     */
    ProductResponse restock(
            UUID id,
            ProductRestockRequest request
    );

    /**
     * Elimina lógicamente un producto.
     *
     * <p>El producto permanece almacenado para conservar su trazabilidad
     * e historial.</p>
     *
     * @param id identificador único del producto
     */
    void delete(UUID id);

    /**
     * Descuenta las cantidades indicadas del stock de múltiples productos.
     *
     * <p>La operación valida los elementos de la solicitud y verifica
     * que cada producto disponga de stock suficiente para efectuar
     * el descuento solicitado.</p>
     *
     * @param request lista de identificadores de productos y cantidades
     *                que se deben descontar
     * @return lista de productos con sus existencias actualizadas
     * @throws IllegalArgumentException si la solicitud es inválida,
     *         contiene productos duplicados o existe stock insuficiente
     */
    List<ProductResponse> bulkReduceStock(
            List<ProductStockReductionItemRequest> request
    );
}
