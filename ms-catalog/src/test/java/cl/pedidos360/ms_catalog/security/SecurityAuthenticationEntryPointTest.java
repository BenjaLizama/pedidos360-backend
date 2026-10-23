
package cl.pedidos360.ms_catalog.security;

import cl.pedidos360.ms_catalog.dto.response.StandardErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SecurityAuthenticationEntryPointTest {

    private ObjectMapper objectMapper;
    private SecurityAuthenticationEntryPoint entryPoint;

    @BeforeEach
    void setUp() {
        objectMapper = mock(ObjectMapper.class);
        entryPoint = new SecurityAuthenticationEntryPoint(objectMapper);
    }

    @Test
    void shouldReturnUnauthorizedResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/private");

        MockHttpServletResponse response = new MockHttpServletResponse();

        BadCredentialsException exception =
                new BadCredentialsException("Invalid credentials");

        entryPoint.commence(request, response, exception);

        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());

        verify(objectMapper).writeValue(
                eq(response.getOutputStream()),
                any(StandardErrorResponse.class)
        );
    }

    @Test
    void shouldWriteExpectedErrorDetails() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/private");

        MockHttpServletResponse response = new MockHttpServletResponse();

        BadCredentialsException exception =
                new BadCredentialsException("Invalid token");

        entryPoint.commence(request, response, exception);

        var captor =
                org.mockito.ArgumentCaptor.forClass(StandardErrorResponse.class);

        verify(objectMapper).writeValue(
                eq(response.getOutputStream()),
                captor.capture()
        );

        StandardErrorResponse error = captor.getValue();

        assertEquals(401, error.status());
        assertEquals("AUTHENTICATION_REQUIRED", error.code());
        assertEquals("UNAUTHORIZED", error.error());
        assertEquals(
                "Se requiere autenticación para acceder a este recurso.",
                error.message()
        );
        assertEquals("Invalid token", error.developerMessage());
        assertEquals("/api/v1/private", error.path());
        assertTrue(error.timestamp() > 0);
    }
}
