package cl.pedidos360.ms_catalog.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro encargado de establecer el identificador de correlación
 * de cada solicitud HTTP.
 *
 * <p>El correlation ID permite relacionar todos los logs generados
 * durante el procesamiento de una misma solicitud.</p>
 *
 * <p>Si el cliente proporciona un {@code X-Correlation-ID} válido,
 * se reutiliza. En caso contrario, se genera uno nuevo.</p>
 */
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Correlation-ID";
    public static final String MDC_KEY = "CorrelationId";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String correlationalId = resolveCorrelationId(request);

        MDC.put(MDC_KEY, correlationalId);
        response.setHeader(HEADER_NAME, correlationalId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    /**
     * Obtiene el correlation ID enviado por el cliente o genera uno nuevo.
     */
    private String resolveCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(HEADER_NAME);

        if (correlationId == null || correlationId.isBlank()) {
            return UUID.randomUUID().toString();
        }

        try {
            return UUID.fromString(correlationId).toString();
        } catch (IllegalArgumentException exception) {
            return UUID.randomUUID().toString();
        }
    }
}
