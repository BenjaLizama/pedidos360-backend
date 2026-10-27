package cl.pedidos360.ms_catalog.service.impl;

import cl.pedidos360.ms_catalog.dto.response.PriceHistoryResponse;
import cl.pedidos360.ms_catalog.mapper.PriceHistoryMapper;
import cl.pedidos360.ms_catalog.observability.CorrelationIdProvider;
import cl.pedidos360.ms_catalog.repository.PriceHistoryRepository;
import cl.pedidos360.ms_catalog.service.PriceHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implementación del servicio encargado de consultar
 * el historial de precios de los productos.
 *
 * <p>Este servicio se limita a operaciones de consulta.
 * La creación de registros de historial será responsabilidad
 * de la operación de actualización de precio del producto.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceHistoryServiceImpl implements PriceHistoryService {

    private final PriceHistoryRepository priceHistoryRepository;
    private final PriceHistoryMapper priceHistoryMapper;
    private final CorrelationIdProvider correlationIdProvider;

    /**
     * Obtiene el historial de precios de un producto utilizando
     * paginación y ordenamiento.
     *
     * @param productId identificador del producto
     * @param pageable configuración de paginación y ordenamiento
     * @return página de registros históricos de precio
     */
    @Override
    public Page<PriceHistoryResponse> findByProductId(UUID productId, Pageable pageable) {
        UUID correlationId = correlationIdProvider.getCorrelationId();

        log.debug(
                "Consultando historial de precios " +
                "| productId={} | page={} | size={} | correlationId={}",
                productId,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                correlationId
        );

        return priceHistoryRepository
                .findByProductId(productId, pageable)
                .map(priceHistoryMapper::toResponse);
    }
}
