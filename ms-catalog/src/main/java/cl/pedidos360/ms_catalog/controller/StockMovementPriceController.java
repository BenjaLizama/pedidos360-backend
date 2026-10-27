package cl.pedidos360.ms_catalog.controller;

import cl.pedidos360.ms_catalog.controller.docs.StockMovementControllerDocs;
import cl.pedidos360.ms_catalog.dto.response.StandardResponse;
import cl.pedidos360.ms_catalog.dto.response.StockMovementResponse;
import cl.pedidos360.ms_catalog.service.StockMovementService;
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
@RequestMapping("/api/v1/stock-movement")
@RequiredArgsConstructor
public class StockMovementPriceController implements StockMovementControllerDocs {

    private final StockMovementService stockMovementService;

    @Override
    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<StandardResponse<Page<StockMovementResponse>>> findByProductId(
            @PathVariable UUID productId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<StockMovementResponse> response = stockMovementService.findByProductId(productId, pageable);

        return ResponseEntity.ok(StandardResponse.ok(
                "Histórico de movimiento de stock obtenido con exito.",
                response
        ));
    }
}
