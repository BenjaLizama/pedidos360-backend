package cl.pedidos360.ms_catalog.service;

import cl.pedidos360.ms_catalog.dto.response.StockMovementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Servicio encargado de consultar los movimientos de inventario
 * registrados para los productos del catálogo.
 *
 * <p>Este servicio expone operaciones de consulta, mientras que la
 * creación de movimientos pertenece a las operaciones internas que
 * modifican el stock.</p>
 */
public interface StockMovementService {

    /**
     * Obtiene los movimientos de inventario asociados a un producto
     * de forma paginada.
     *
     * @param productId identificador único del producto
     * @param pageable configuración de paginación y ordenamiento
     * @return página con los movimientos de inventario del producto
     */
    Page<StockMovementResponse> findByProductId(
            UUID productId,
            Pageable pageable
    );
}
