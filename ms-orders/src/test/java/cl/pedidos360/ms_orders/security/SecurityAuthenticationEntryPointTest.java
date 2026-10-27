package cl.pedidos360.ms_orders.security;

import cl.pedidos360.ms_orders.dto.response.StandardErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityAuthenticationEntryPointTest {

    private ObjectMapper objectMapper;
    private SecurityAuthenticationEntryPoint entryPoint;
    private HttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        entryPoint = new SecurityAuthenticationEntryPoint(objectMapper);

        request = mock(HttpServletRequest.class);
        response = new MockHttpServletResponse();
    }

    @Test
    void commence_returnsUnauthorizedResponse() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/orders");

        BadCredentialsException exception =
                new BadCredentialsException("Token inválido");

        entryPoint.commence(request, response, exception);

        assertEquals(
                HttpServletResponse.SC_UNAUTHORIZED,
                response.getStatus()
        );
        assertEquals("application/json", response.getContentType());

        StandardErrorResponse body = objectMapper.readValue(
                response.getContentAsByteArray(),
                StandardErrorResponse.class
        );

        assertNotNull(body);
        assertEquals(401, body.status());
        assertEquals("AUTHENTICATION_REQUIRED", body.code());
        assertEquals("UNAUTHORIZED", body.error());
        assertEquals(
                "Se requiere autenticación para acceder a este recurso.",
                body.message()
        );
        assertEquals("Token inválido", body.developerMessage());
        assertEquals("/api/v1/orders", body.path());
        assertNotNull(body.timestamp());
    }
}
