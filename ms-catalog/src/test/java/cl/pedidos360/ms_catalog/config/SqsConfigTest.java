package cl.pedidos360.ms_catalog.config;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class SqsConfigTest {

    @Test
    void shouldCreateSqsTemplate() {
        SqsConfig config = new SqsConfig();
        SqsAsyncClient sqsAsyncClient = mock(SqsAsyncClient.class);

        SqsTemplate result = config.sqsTemplate(sqsAsyncClient);

        assertNotNull(result);
    }
}
