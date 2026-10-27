package cl.pedidos360.ms_catalog.security;

import cl.pedidos360.ms_catalog.dto.response.StandardErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Handler encargado de construir la respuesta HTTP cuando un usuario
 * autenticado intenta acceder a un recurso sin los permisos necesarios.
 *
 * <p>Representa los errores de autorización HTTP {@code 403 Forbidden}
 * producidos por Spring Security.</p>
 *
 * <p>La respuesta utiliza el mismo formato estandarizado empleado por
 * el resto de la API mediante {@link StandardErrorResponse}.</p>
 */
@Component
@RequiredArgsConstructor
public class SecurityAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * Mapper utilizado para serializar la respuesta de error
     * directamente en el cuerpo de la respuesta HTTP.
     */
    private final ObjectMapper objectMapper;

    /**
     * Procesa un error de autorización y devuelve una respuesta
     * HTTP 403 Forbidden.
     *
     * <p>Este handler se ejecuta cuando la identidad del usuario ha sido
     * correctamente establecida, pero sus autoridades no permiten
     * acceder al recurso solicitado.</p>
     *
     * @param request solicitud HTTP que originó el error
     * @param response respuesta HTTP que será enviada al cliente
     * @param accessDeniedException excepción generada por Spring Security
     * @throws IOException si ocurre un error al escribir la respuesta
     * @throws ServletException si ocurre un error relacionado con
     *                          el procesamiento de la solicitud
     */
    @Override
    public void handle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        StandardErrorResponse errorResponse = StandardErrorResponse.builder()
                .status(HttpServletResponse.SC_FORBIDDEN)
                .code("ACCESS_DENIED")
                .error("FORBIDDEN")
                .message("No tiene permiso para acceder a este recurso.")
                .developerMessage(accessDeniedException.getMessage())
                .path(request.getRequestURI())
                .timestamp(System.currentTimeMillis())
                .build();

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(
                response.getOutputStream(),
                errorResponse
        );
    }
}
