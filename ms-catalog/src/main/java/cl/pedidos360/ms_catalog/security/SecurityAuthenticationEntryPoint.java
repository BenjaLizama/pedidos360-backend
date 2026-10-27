package cl.pedidos360.ms_catalog.security;

import cl.pedidos360.ms_catalog.dto.response.StandardErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Entry point encargado de construir la respuesta HTTP cuando una
 * solicitud requiere autenticación y el usuario no se encuentra
 * correctamente autenticado.
 *
 * <p>Representa los errores de autenticación HTTP {@code 401 Unauthorized}
 * producidos por Spring Security.</p>
 *
 * <p>La respuesta utiliza {@link StandardErrorResponse} para mantener
 * el mismo contrato de errores utilizado por el resto de la API.</p>
 */
@Component
@RequiredArgsConstructor
public class SecurityAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Mapper utilizado para serializar la respuesta de error
     * directamente en el cuerpo de la respuesta HTTP.
     */
    private final ObjectMapper objectMapper;

    /**
     * Procesa un error de autenticación y devuelve una respuesta
     * HTTP 401 Unauthorized.
     *
     * <p>Este método se utiliza cuando la solicitud no contiene una
     * autenticación válida o cuando el token proporcionado no permite
     * establecer correctamente la identidad del usuario.</p>
     *
     * @param request solicitud HTTP que originó el error
     * @param response respuesta HTTP que será enviada al cliente
     * @param authException excepción de autenticación generada por
     *                      Spring Security
     * @throws IOException si ocurre un error al escribir la respuesta
     * @throws ServletException si ocurre un error relacionado con
     *                          el procesamiento de la solicitud
     */
    @Override
    public void commence(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AuthenticationException authException
    ) throws IOException, ServletException {
        StandardErrorResponse errorResponse = StandardErrorResponse.builder()
                .status(HttpServletResponse.SC_UNAUTHORIZED)
                .code("AUTHENTICATION_REQUIRED")
                .error("UNAUTHORIZED")
                .message("Se requiere autenticación para acceder a este recurso.")
                .developerMessage(authException.getMessage())
                .path(request.getRequestURI())
                .timestamp(System.currentTimeMillis())
                .build();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(
                response.getOutputStream(),
                errorResponse
        );
    }
}
