package cl.pedidos360.ms_catalog.controller;

import cl.pedidos360.ms_catalog.controller.docs.PriceHistoryControllerDocs;
import cl.pedidos360.ms_catalog.dto.response.PriceHistoryResponse;
import cl.pedidos360.ms_catalog.dto.response.StandardResponse;
import cl.pedidos360.ms_catalog.service.PriceHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/price-history")
@RequiredArgsConstructor
public class PriceHistoryController implements PriceHistoryControllerDocs {

    private final PriceHistoryService priceHistoryService;

    @Override
    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<StandardResponse<Page<PriceHistoryResponse>>> findByProductId(
            @PathVariable UUID productId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<PriceHistoryResponse> response = priceHistoryService.findByProductId(productId, pageable);

        return ResponseEntity.ok(StandardResponse.ok(
                "Historial de precios recuperado con éxito.",
                response
        ));
    }
}
