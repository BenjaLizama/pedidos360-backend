
package cl.pedidos360.bff.controller;

import cl.pedidos360.bff.client.CatalogClient;
import cl.pedidos360.bff.controller.docs.PriceHistoryControllerDocs;
import cl.pedidos360.bff.dto.response.PageResponse;
import cl.pedidos360.bff.dto.response.PriceHistoryResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/price-history")
@RequiredArgsConstructor
public class PriceHistoryController implements PriceHistoryControllerDocs {

    private final CatalogClient catalogClient;

    @Override
    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<PriceHistoryResponse> findByProductId(
            @PathVariable UUID productId,
            @RequestParam Map<String, String> queryParams
    ) {
        return catalogClient.getPriceHistory(productId, queryParams)
                .data();
    }
}
