
package cl.pedidos360.ms_catalog.security;

import cl.pedidos360.ms_catalog.dto.response.StandardErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SecurityAccessDeniedHandlerTest {

    private ObjectMapper objectMapper;
    private SecurityAccessDeniedHandler handler;

    @BeforeEach
    void setUp() {
        objectMapper = mock(ObjectMapper.class);
        handler = new SecurityAccessDeniedHandler(objectMapper);
    }

    @Test
    void shouldReturnForbiddenResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/admin");

        MockHttpServletResponse response = new MockHttpServletResponse();

        AccessDeniedException exception =
                new AccessDeniedException("Insufficient permissions");

        handler.handle(request, response, exception);

        assertEquals(403, response.getStatus());
        assertEquals("application/json", response.getContentType());

        verify(objectMapper).writeValue(
                eq(response.getOutputStream()),
                any(StandardErrorResponse.class)
        );
    }

    @Test
    void shouldWriteExpectedErrorDetails() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/admin");

        MockHttpServletResponse response = new MockHttpServletResponse();

        AccessDeniedException exception =
                new AccessDeniedException("Access denied for test");

        handler.handle(request, response, exception);

        var captor =
                org.mockito.ArgumentCaptor.forClass(StandardErrorResponse.class);

        verify(objectMapper).writeValue(
                eq(response.getOutputStream()),
                captor.capture()
        );

        StandardErrorResponse error = captor.getValue();

        assertEquals(403, error.status());
        assertEquals("ACCESS_DENIED", error.code());
        assertEquals("FORBIDDEN", error.error());
        assertEquals(
                "No tiene permiso para acceder a este recurso.",
                error.message()
        );
        assertEquals("Access denied for test", error.developerMessage());
        assertEquals("/api/v1/admin", error.path());
        assertTrue(error.timestamp() > 0);
    }
}
