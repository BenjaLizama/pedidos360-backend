package cl.eventpass.bff.controller;

import cl.eventpass.bff.controller.docs.StockMovementControllerDocs;
import cl.eventpass.bff.dto.response.StockMovementResponse;
import org.springframework.data.domain.Page;

import java.util.Map;
import java.util.UUID;

public class StockMovementController implements StockMovementControllerDocs {

    @Override
    public Page<StockMovementResponse> getStockMovements(UUID productId, Map<String, String> queryParams) {
        return null;
    }
}
