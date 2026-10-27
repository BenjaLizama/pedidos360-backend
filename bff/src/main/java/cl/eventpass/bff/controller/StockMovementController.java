
package cl.eventpass.bff.controller;

import cl.eventpass.bff.client.CatalogClient;
import cl.eventpass.bff.controller.docs.StockMovementControllerDocs;
import cl.eventpass.bff.dto.response.PageResponse;
import cl.eventpass.bff.dto.response.StandardResponse;
import cl.eventpass.bff.dto.response.StockMovementResponse;

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

/**
 * Controlador BFF para consultar los movimientos de stock
 * registrados para un producto.
 *
 * Las consultas se delegan al microservicio de catálogo.
 */
@RestController
@RequestMapping("/api/v1/stock-movement")
@RequiredArgsConstructor
public class StockMovementController implements StockMovementControllerDocs {

    private final CatalogClient catalogClient;

    /**
     * Obtiene el historial paginado de movimientos de stock de un producto.
     *
     * @param productId identificador único del producto
     * @param queryParams parámetros de paginación, ordenamiento y filtros
     * @return respuesta estándar con los movimientos y sus metadatos de paginación
     */
    @Override
    @GetMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public StandardResponse<PageResponse<StockMovementResponse>> getStockMovements(
            @PathVariable UUID productId,
            @RequestParam Map<String, String> queryParams
    ) {
        return catalogClient.getStockMovements(productId, queryParams);
    }
}
