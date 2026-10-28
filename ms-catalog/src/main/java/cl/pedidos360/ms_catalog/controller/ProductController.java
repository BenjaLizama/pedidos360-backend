package cl.pedidos360.ms_catalog.controller;

import cl.pedidos360.ms_catalog.controller.docs.ProductControllerDocs;
import cl.pedidos360.ms_catalog.dto.request.*;
import cl.pedidos360.ms_catalog.dto.response.ProductResponse;
import cl.pedidos360.ms_catalog.dto.response.StandardResponse;
import cl.pedidos360.ms_catalog.enums.ProductStatus;
import cl.pedidos360.ms_catalog.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController implements ProductControllerDocs {

    private final ProductService productService;

    @Override
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<StandardResponse<ProductResponse>> create(
            @RequestBody ProductCreateRequest request
    ) {
        ProductResponse response = productService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(StandardResponse.created(
                        "Producto creado con éxito.",
                        response
                ));
    }

    @Override
    @GetMapping("/{productId}")
    public ResponseEntity<StandardResponse<ProductResponse>> findById(
            @PathVariable UUID productId
    ) {
        ProductResponse response = productService.findById(productId);

        return ResponseEntity.ok(StandardResponse.ok(
                "Producto recuperado con éxito.",
                response
        ));
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<StandardResponse<Page<ProductResponse>>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) ProductStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ProductResponse> response =
                productService.search(name, categoryId, status, pageable);

        return ResponseEntity.ok(StandardResponse.ok(
                "Productos recuperados con éxito.",
                response
        ));
    }

    @Override
    @PutMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<StandardResponse<ProductResponse>> update(
            @PathVariable UUID productId,
            @RequestBody ProductUpdateRequest request
    ) {
        ProductResponse response = productService.update(productId, request);

        return ResponseEntity.ok(StandardResponse.ok(
                "Producto actualizado con éxito.",
                response
        ));
    }

    @Override
    @PatchMapping("/{productId}/status")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<StandardResponse<ProductResponse>> updateStatus(
            @PathVariable UUID productId,
            @RequestBody ProductStatusUpdateRequest request
    ) {
        ProductResponse response = productService.updateStatus(productId, request);

        return ResponseEntity.ok(StandardResponse.ok(
                "Estado del producto actualizado con éxito.",
                response
        ));
    }

    @Override
    @PatchMapping("/{productId}/price")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<StandardResponse<ProductResponse>> updatePrice(
            @PathVariable UUID productId,
            @RequestBody ProductPriceUpdateRequest request
    ) {
        ProductResponse response = productService.updatePrice(productId, request);

        return ResponseEntity.ok(StandardResponse.ok(
                "Precio del producto actualizado con éxito.",
                response
        ));
    }

    @Override
    @PatchMapping("/{productId}/stock")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<StandardResponse<ProductResponse>> updateStock(
            @PathVariable UUID productId,
            @RequestBody ProductStockUpdateRequest request
    ) {
        ProductResponse response = productService.updateStock(productId, request);

        return ResponseEntity.ok(StandardResponse.ok(
                "Stock del producto actualizado con éxito.",
                response
        ));
    }

    @Override
    @PatchMapping("/{productId}/restock")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<StandardResponse<ProductResponse>> restock(
            @PathVariable UUID productId,
            @RequestBody ProductRestockRequest request
    ) {
        ProductResponse response = productService.restock(productId, request);

        return ResponseEntity.ok(StandardResponse.ok(
                "Stock del producto incrementado con éxito.",
                response
        ));
    }

    @Override
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<StandardResponse<Void>> delete(
            @PathVariable UUID productId
    ) {
        productService.delete(productId);

        return ResponseEntity.ok(StandardResponse.ok(
                "Producto eliminado con éxito.",
                null
        ));
    }

    @Override
    @PostMapping("/bulk/reduce-stock")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<StandardResponse<Void>> bulkReduceStock(
            @RequestBody List<ProductStockReductionItemRequest> request
    ) {
        productService.bulkReduceStock(request);

        return ResponseEntity.ok(
                StandardResponse.ok("Stock actualizado correctamente", null)
        );
    }
}
