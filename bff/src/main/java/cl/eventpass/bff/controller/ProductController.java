package cl.eventpass.bff.controller;

import cl.eventpass.bff.client.CatalogClient;
import cl.eventpass.bff.controller.docs.ProductControllerDocs;
import cl.eventpass.bff.dto.request.BulkStockReductionItemRequest;
import cl.eventpass.bff.dto.request.ProductCreateRequest;
import cl.eventpass.bff.dto.request.ProductPriceUpdateRequest;
import cl.eventpass.bff.dto.request.ProductRestockRequest;
import cl.eventpass.bff.dto.request.ProductStatusUpdateRequest;
import cl.eventpass.bff.dto.request.ProductStockUpdateRequest;
import cl.eventpass.bff.dto.request.ProductUpdateRequest;
import cl.eventpass.bff.dto.response.PageResponse;
import cl.eventpass.bff.dto.response.ProductResponse;
import cl.eventpass.bff.dto.response.StandardResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController implements ProductControllerDocs {

    private final CatalogClient catalogClient;

    /**
     * Crea un producto mediante el microservicio de catálogo.
     */
    @Override
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public StandardResponse<ProductResponse> createProduct(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        return catalogClient.createProduct(request);
    }

    /**
     * Obtiene un producto por su identificador.
     */
    @Override
    @GetMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public StandardResponse<ProductResponse> getProductById(
            @PathVariable UUID productId
    ) {
        return catalogClient.getProductById(productId);
    }

    /**
     * Busca productos utilizando filtros y parámetros de paginación.
     */
    @Override
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public StandardResponse<PageResponse<ProductResponse>> search(
            @RequestParam Map<String, String> queryParams
    ) {
        return catalogClient.search(queryParams);
    }

    /**
     * Actualiza los datos generales de un producto.
     */
    @Override
    @PutMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    @ResponseStatus(HttpStatus.OK)
    public StandardResponse<ProductResponse> updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        return catalogClient.updateProduct(productId, request);
    }

    /**
     * Actualiza el estado de un producto.
     */
    @Override
    @PatchMapping("/{productId}/status")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    @ResponseStatus(HttpStatus.OK)
    public StandardResponse<ProductResponse> updateProductStatus(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductStatusUpdateRequest request
    ) {
        return catalogClient.updateProductStatus(productId, request);
    }

    /**
     * Actualiza el precio de un producto.
     */
    @Override
    @PatchMapping("/{productId}/price")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    @ResponseStatus(HttpStatus.OK)
    public StandardResponse<ProductResponse> updateProductPrice(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductPriceUpdateRequest request
    ) {
        return catalogClient.updateProductPrice(productId, request);
    }

    /**
     * Ajusta el stock de un producto.
     */
    @Override
    @PatchMapping("/{productId}/stock")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    @ResponseStatus(HttpStatus.OK)
    public StandardResponse<ProductResponse> updateProductStock(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductStockUpdateRequest request
    ) {
        return catalogClient.updateProductStock(productId, request);
    }

    /**
     * Repone el stock de un producto.
     */
    @Override
    @PatchMapping("/{productId}/restock")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    @ResponseStatus(HttpStatus.OK)
    public StandardResponse<ProductResponse> restockProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductRestockRequest request
    ) {
        return catalogClient.restockProduct(productId, request);
    }

    /**
     * Solicita la eliminación de un producto.
     */
    @Override
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(
            @PathVariable UUID productId
    ) {
        catalogClient.deleteProduct(productId);
    }

    /**
     * Solicita una reducción masiva del stock de varios productos.
     */
    @Override
    @PostMapping("/bulk/reduce-stock")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    @ResponseStatus(HttpStatus.OK)
    public StandardResponse<?> bulkReduceStock(
            @Valid @RequestBody List<@Valid BulkStockReductionItemRequest> request
    ) {
        return catalogClient.bulkReduceStock(request);
    }
}
