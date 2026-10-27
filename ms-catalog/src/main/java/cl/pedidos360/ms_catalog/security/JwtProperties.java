package cl.pedidos360.ms_catalog.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de configuración relacionadas con la autenticación JWT.
 *
 * <p>Las propiedades se obtienen desde la configuración de Spring
 * utilizando el prefijo {@code security.jwt}.</p>
 *
 * <p>Estas propiedades permiten adaptar la extracción de roles desde
 * diferentes proveedores de identidad sin acoplar la aplicación a un
 * proveedor específico.</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /**
     * Ruta o nombre del claim utilizado para obtener los roles
     * contenidos en el token JWT.
     *
     * <p>Por defecto, el proyecto utiliza {@code realm_access.roles}
     * debido a la estructura de los tokens emitidos por Keycloak.</p>
     */
    private String rolesClaim;

    /**
     * Prefijo aplicado a cada rol antes de convertirlo en una autoridad
     * de Spring Security.
     *
     * <p>Por ejemplo, el rol {@code ADMIN} se transforma en
     * {@code ROLE_ADMIN} cuando el prefijo configurado es
     * {@code ROLE_}.</p>
     */
    private String rolesPrefix;
}
