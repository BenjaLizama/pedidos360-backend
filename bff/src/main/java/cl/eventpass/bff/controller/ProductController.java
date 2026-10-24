package cl.eventpass.bff.controller;

import cl.eventpass.bff.controller.docs.ProductControllerDocs;
import cl.eventpass.bff.dto.request.*;
import cl.eventpass.bff.dto.response.ProductResponse;
import cl.eventpass.bff.dto.response.StandardResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProductController implements ProductControllerDocs {

    @Override
    public StandardResponse<ProductResponse> createProduct(ProductCreateRequest request) {
        return null;
    }

    @Override
    public StandardResponse<ProductResponse> getProductById(UUID productId) {
        return null;
    }

    @Override
    public StandardResponse<Page<ProductResponse>> search(Map<String, String> queryParams) {
        return null;
    }

    @Override
    public StandardResponse<ProductResponse> updateProduct(UUID productId, ProductUpdateRequest request) {
        return null;
    }

    @Override
    public StandardResponse<ProductResponse> updateProductStatus(UUID productId, ProductStatusUpdateRequest request) {
        return null;
    }

    @Override
    public StandardResponse<ProductResponse> updateProductPrice(UUID productId, ProductPriceUpdateRequest request) {
        return null;
    }

    @Override
    public StandardResponse<ProductResponse> updateProductStock(UUID productId, ProductStockUpdateRequest request) {
        return null;
    }

    @Override
    public StandardResponse<ProductResponse> restockProduct(UUID productId, ProductRestockRequest request) {
        return null;
    }

    @Override
    public void deleteProduct(UUID productId) {

    }

    @Override
    public StandardResponse<?> bulkReduceStock(List<BulkStockReductionItemRequest> request) {
        return null;
    }
}
