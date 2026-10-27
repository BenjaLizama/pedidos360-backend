package cl.pedidos360.ms_orders.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CatalogClientTest {

    private MockRestServiceServer server;
    private CatalogClient catalogClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();

        server = MockRestServiceServer.bindTo(builder).build();

        catalogClient = new CatalogClient(
                builder,
                "http://localhost:8081"
        );

        Jwt jwt = Jwt.withTokenValue("test-access-token")
                .header("alg", "none")
                .subject("test-user")
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(jwt)
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void findById_withoutJwt_throwsIllegalStateException() {
        SecurityContextHolder.clearContext();

        assertThrows(
                IllegalStateException.class,
                () -> catalogClient.findById(UUID.randomUUID())
        );
    }

    @Test
    void reduceStock_whenItemsAreNull_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> catalogClient.reduceStock(null)
        );
    }

    @Test
    void reduceStock_whenItemsAreEmpty_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> catalogClient.reduceStock(List.of())
        );
    }

    @Test
    void findById_withoutAuthenticatedJwt_throwsIllegalStateException() {
        SecurityContextHolder.getContext().setAuthentication(null);

        assertThrows(
                IllegalStateException.class,
                () -> catalogClient.findById(UUID.randomUUID())
        );
    }
}
