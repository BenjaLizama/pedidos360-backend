package cl.pedidos360.ms_catalog.service.impl;

import cl.pedidos360.ms_catalog.dto.response.StockMovementResponse;
import cl.pedidos360.ms_catalog.mapper.StockMovementMapper;
import cl.pedidos360.ms_catalog.observability.CorrelationIdProvider;
import cl.pedidos360.ms_catalog.repository.StockMovementRepository;
import cl.pedidos360.ms_catalog.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implementación del servicio encargado de consultar
 * los movimientos de stock de los productos.
 *
 * <p>Este servicio se limita a operaciones de consulta.
 * La creación de movimientos será responsabilidad de las
 * operaciones de gestión de inventario.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final StockMovementMapper stockMovementMapper;
    private final CorrelationIdProvider correlationIdProvider;

    /**
     * Obtiene los movimientos de stock de un producto
     * utilizando paginación y ordenamiento.
     *
     * @param productId identificador del producto
     * @param pageable configuración de paginación y ordenamiento
     * @return página de movimientos de stock
     */
    @Override
    public Page<StockMovementResponse> findByProductId(UUID productId, Pageable pageable) {
        UUID correlationId = correlationIdProvider.getCorrelationId();

        log.debug(
                "Consultando movimientos de stock " +
                "| productId={} | page={} | size={} | correlationId={}",
                productId,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                correlationId
        );

        return stockMovementRepository
                .findByProductId(productId, pageable)
                .map(stockMovementMapper::toResponse);
    }
}
