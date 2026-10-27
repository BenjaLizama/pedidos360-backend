package cl.pedidos360.ms_orders.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de la documentación OpenAPI del microservicio ms-orders.
 *
 * Define los metadatos generales de la API y registra el esquema de
 * autenticación Bearer mediante tokens JWT.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pedidos360OrdersOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pedidos360 Orders API")
                        .version("1.0.0")
                        .description("""
                                API REST del sistema Pedidos360.

                                Este servicio gestiona el ciclo de vida de las órdenes
                                de compra, incluyendo su creación, consulta, cancelación
                                y actualización de estado.

                                Permite a los clientes consultar sus propias órdenes,
                                mientras que los operadores y administradores pueden
                                consultar y gestionar las órdenes según sus permisos.
                                """)
                )
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(
                                                "Ingrese el token JWT emitido por Keycloak."
                                        )
                        )
                )
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("bearerAuth")
                );
    }
}
