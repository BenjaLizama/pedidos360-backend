package cl.pedidos360.ms_catalog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    @Test
    void shouldConfigureApiInformation() {
        OpenAPI openAPI = new OpenApiConfig().pedidos360CatalogOpenAPI();

        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertEquals("Pedidos360 Catalog API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
        assertTrue(openAPI.getInfo().getDescription().contains("catálogo"));
    }

    @Test
    void shouldConfigureJwtBearerSecurityScheme() {
        OpenAPI openAPI = new OpenApiConfig().pedidos360CatalogOpenAPI();

        assertNotNull(openAPI.getComponents());

        SecurityScheme scheme = openAPI.getComponents()
                .getSecuritySchemes()
                .get("bearerAuth");

        assertNotNull(scheme);
        assertEquals(SecurityScheme.Type.HTTP, scheme.getType());
        assertEquals("bearer", scheme.getScheme());
        assertEquals("JWT", scheme.getBearerFormat());
        assertEquals("Ingrese el token JWT.", scheme.getDescription());
    }
}
