package cl.pedidos360.ms_catalog.observability;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CorrelationIdFilterTest {

    private CorrelationIdFilter correlationIdFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        correlationIdFilter = new CorrelationIdFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        MDC.remove(CorrelationIdFilter.MDC_KEY);
    }

    @AfterEach
    void tearDown() {
        MDC.remove(CorrelationIdFilter.MDC_KEY);
    }

    @Test
    void shouldReuseValidCorrelationIdFromRequest()
            throws ServletException, IOException {

        UUID expectedId = UUID.randomUUID();
        request.addHeader(
                CorrelationIdFilter.HEADER_NAME,
                expectedId.toString()
        );

        correlationIdFilter.doFilter(
                request,
                response,
                (servletRequest, servletResponse) -> {
                    assertEquals(
                            expectedId.toString(),
                            MDC.get(CorrelationIdFilter.MDC_KEY)
                    );
                }
        );

        assertEquals(
                expectedId.toString(),
                response.getHeader(CorrelationIdFilter.HEADER_NAME)
        );

        assertNull(MDC.get(CorrelationIdFilter.MDC_KEY));
    }

    @Test
    void shouldGenerateCorrelationIdWhenHeaderIsMissing()
            throws ServletException, IOException {

        executeFilterAndAssertGeneratedId();
    }

    @Test
    void shouldGenerateCorrelationIdWhenHeaderIsBlank()
            throws ServletException, IOException {

        request.addHeader(CorrelationIdFilter.HEADER_NAME, "   ");

        executeFilterAndAssertGeneratedId();
    }

    @Test
    void shouldGenerateCorrelationIdWhenHeaderIsInvalid()
            throws ServletException, IOException {

        request.addHeader(
                CorrelationIdFilter.HEADER_NAME,
                "invalid-uuid"
        );

        executeFilterAndAssertGeneratedId();
    }

    @Test
    void shouldNormalizeValidUuidHeader()
            throws ServletException, IOException {

        UUID expectedId = UUID.randomUUID();
        String uppercaseId = expectedId.toString().toUpperCase();

        request.addHeader(
                CorrelationIdFilter.HEADER_NAME,
                uppercaseId
        );

        correlationIdFilter.doFilter(
                request,
                response,
                (servletRequest, servletResponse) -> {
                    assertEquals(
                            expectedId.toString(),
                            MDC.get(CorrelationIdFilter.MDC_KEY)
                    );
                }
        );

        assertEquals(
                expectedId.toString(),
                response.getHeader(CorrelationIdFilter.HEADER_NAME)
        );
    }

    @Test
    void shouldRemoveCorrelationIdFromMdcWhenFilterChainThrows()
            throws ServletException, IOException {

        request.addHeader(
                CorrelationIdFilter.HEADER_NAME,
                UUID.randomUUID().toString()
        );

        assertThrows(
                ServletException.class,
                () -> correlationIdFilter.doFilter(
                        request,
                        response,
                        (servletRequest, servletResponse) -> {
                            assertNotNull(
                                    MDC.get(CorrelationIdFilter.MDC_KEY)
                            );

                            throw new ServletException("Error de prueba");
                        }
                )
        );

        assertNull(MDC.get(CorrelationIdFilter.MDC_KEY));
    }

    private void executeFilterAndAssertGeneratedId()
            throws ServletException, IOException {

        correlationIdFilter.doFilter(
                request,
                response,
                (servletRequest, servletResponse) -> {
                    String correlationId =
                            MDC.get(CorrelationIdFilter.MDC_KEY);

                    assertNotNull(correlationId);
                    assertDoesNotThrow(
                            () -> UUID.fromString(correlationId)
                    );

                    assertEquals(
                            correlationId,
                            response.getHeader(
                                    CorrelationIdFilter.HEADER_NAME
                            )
                    );
                }
        );

        assertNull(MDC.get(CorrelationIdFilter.MDC_KEY));
    }
}
