package cl.pedidos360.ms_catalog.mapper;

import cl.pedidos360.ms_catalog.dto.request.ProductCreateRequest;
import cl.pedidos360.ms_catalog.dto.request.ProductUpdateRequest;
import cl.pedidos360.ms_catalog.dto.response.ProductResponse;
import cl.pedidos360.ms_catalog.entity.ProductEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper responsable de transformar objetos relacionados con productos.
 * <p>
 * No contiene reglas de negocio.
 * Su única responsabilidad es convertir DTOs en entidades y entidades
 * en DTOs de respuesta.
 */
@Component
public class ProductMapper {

    /**
     * Convierte la información de creación de un producto
     * en una nueva entidad.
     * <p>
     * Los campos generados por el sistema, como ID, auditoría
     * y versión, no se establecen aquí.
     */
    public ProductEntity toEntity(ProductCreateRequest request) {

        ProductEntity product = new ProductEntity();

        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setCategoryId(request.categoryId());

        return product;
    }

    /**
     * Actualiza una entidad existente con los datos permitidos
     * por el request de actualización.
     * <p>
     * No modifica SKU, precio, stock ni estado porque esos campos
     * poseen operaciones específicas dentro del dominio.
     */
    public void updateEntity(
            ProductEntity product,
            ProductUpdateRequest request
    ) {

        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategoryId(request.categoryId());
    }

    /**
     * Convierte una entidad de producto en su DTO de respuesta.
     * <p>
     * Los campos internos de persistencia y auditoría que no forman
     * parte del contrato público no se exponen.
     */
    public ProductResponse toResponse(ProductEntity product) {

        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategoryId(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
