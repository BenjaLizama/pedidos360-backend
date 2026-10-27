package cl.pedidos360.ms_catalog.observability;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Proveedor centralizado del identificador de correlación
 * asociado a la solicitud actual.
 */
@Component
public class CorrelationIdProvider {

    /**
     * Obtiene el correlation ID actual.
     *
     * @return UUID de correlación de la solicitud actual
     */
    public UUID getCorrelationId() {
        String correlationId = MDC.get(
                CorrelationIdFilter.MDC_KEY
        );

        if (correlationId == null || correlationId.isBlank()) {
            return null;
        }

        return UUID.fromString(correlationId);
    }
}
