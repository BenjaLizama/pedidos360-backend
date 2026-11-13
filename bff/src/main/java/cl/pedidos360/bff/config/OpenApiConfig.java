package cl.pedidos360.bff.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de la documentación OpenAPI del BFF de Pedidos360.
 *
 * <p>Define la información general de la API y el esquema de seguridad
 * Bearer JWT utilizado para autenticar las solicitudes desde Swagger UI.</p>
 *
 * <p>El token es validado por Spring Security mediante la configuración
 * OAuth2 Resource Server del BFF. Esta clase documenta la seguridad,
 * pero no genera ni valida tokens por sí misma.</p>
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /**
     * Configura la documentación OpenAPI de Pedidos360 BFF.
     *
     * @return configuración OpenAPI con información de la API y seguridad JWT
     */
    @Bean
    public OpenAPI pedidos360BffOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pedidos360 BFF API")
                        .version("v1")
                        .description("""
                                API Backend for Frontend (BFF) de Pedidos360.

                                Centraliza el acceso desde los clientes consumidores
                                hacia los microservicios de catálogo y pedidos.

                                Los endpoints protegidos requieren un token JWT
                                válido emitido por Keycloak.
                                """)
                        .contact(new Contact()
                                .name("Equipo de desarrollo Pedidos360")))
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ))
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(SECURITY_SCHEME_NAME)
                );
    }
}
