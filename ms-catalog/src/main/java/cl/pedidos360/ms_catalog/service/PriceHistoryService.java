package cl.pedidos360.ms_catalog.service;

import cl.pedidos360.ms_catalog.dto.response.PriceHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Servicio encargado de consultar el historial de precios
 * de los productos del catálogo.
 *
 * <p>Este servicio expone únicamente operaciones de consulta,
 * ya que los registros históricos son generados internamente
 * por las operaciones de modificación de precios.</p>
 */
public interface PriceHistoryService {

    /**
     * Obtiene el historial de cambios de precio de un producto
     * de forma paginada.
     *
     * @param productId identificador único del producto
     * @param pageable configuración de paginación y ordenamiento
     * @return página con el historial de precios del producto
     */
    Page<PriceHistoryResponse> findByProductId(
            UUID productId,
            Pageable pageable
    );
}
