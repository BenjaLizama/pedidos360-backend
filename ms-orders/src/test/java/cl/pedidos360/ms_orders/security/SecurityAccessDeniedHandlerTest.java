package cl.pedidos360.ms_orders.security;

import cl.pedidos360.ms_orders.dto.response.StandardErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityAccessDeniedHandlerTest {

    private ObjectMapper objectMapper;
    private SecurityAccessDeniedHandler handler;
    private HttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        handler = new SecurityAccessDeniedHandler(objectMapper);

        request = mock(HttpServletRequest.class);
        response = new MockHttpServletResponse();
    }

    @Test
    void handle_returnsForbiddenResponse() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/orders");

        AccessDeniedException exception =
                new AccessDeniedException("Permisos insuficientes");

        handler.handle(request, response, exception);

        assertEquals(
                HttpServletResponse.SC_FORBIDDEN,
                response.getStatus()
        );
        assertEquals("application/json", response.getContentType());

        StandardErrorResponse body = objectMapper.readValue(
                response.getContentAsByteArray(),
                StandardErrorResponse.class
        );

        assertNotNull(body);
        assertEquals(403, body.status());
        assertEquals("ACCESS_DENIED", body.code());
        assertEquals("FORBIDDEN", body.error());
        assertEquals(
                "No tiene permiso para acceder a este recurso.",
                body.message()
        );
        assertEquals(
                "Permisos insuficientes",
                body.developerMessage()
        );
        assertEquals("/api/v1/orders", body.path());
        assertNotNull(body.timestamp());
    }
}
