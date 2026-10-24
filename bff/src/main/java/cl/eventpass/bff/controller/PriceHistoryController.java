package cl.eventpass.bff.controller;

import cl.eventpass.bff.controller.docs.PriceHistoryControllerDocs;
import cl.eventpass.bff.dto.response.PriceHistoryResponse;
import org.springframework.data.domain.Page;

import java.util.Map;
import java.util.UUID;

public class PriceHistoryController implements PriceHistoryControllerDocs {

    @Override
    public Page<PriceHistoryResponse> findByProductId(UUID productId, Map<String, String> queryParams) {
        return null;
    }
}
