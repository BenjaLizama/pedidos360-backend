package cl.pedidos360.ms_catalog.observability;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CorrelationIdProviderTest {

    private CorrelationIdProvider correlationIdProvider;

    @BeforeEach
    void setUp() {
        correlationIdProvider = new CorrelationIdProvider();
        MDC.remove(CorrelationIdFilter.MDC_KEY);
    }

    @AfterEach
    void tearDown() {
        MDC.remove(CorrelationIdFilter.MDC_KEY);
    }

    @Test
    void shouldReturnCorrelationIdWhenPresent() {
        UUID expectedId = UUID.randomUUID();
        MDC.put(CorrelationIdFilter.MDC_KEY, expectedId.toString());

        UUID result = correlationIdProvider.getCorrelationId();

        assertEquals(expectedId, result);
    }

    @Test
    void shouldReturnNullWhenCorrelationIdIsMissing() {
        UUID result = correlationIdProvider.getCorrelationId();

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenCorrelationIdIsBlank() {
        MDC.put(CorrelationIdFilter.MDC_KEY, "   ");

        UUID result = correlationIdProvider.getCorrelationId();

        assertNull(result);
    }

    @Test
    void shouldThrowExceptionWhenCorrelationIdIsInvalid() {
        MDC.put(CorrelationIdFilter.MDC_KEY, "invalid-uuid");

        assertThrows(
                IllegalArgumentException.class,
                () -> correlationIdProvider.getCorrelationId()
        );
    }
}
