
package cl.pedidos360.ms_catalog.config;

import cl.pedidos360.ms_catalog.security.JwtProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtPropertiesTest {

    @Test
    void shouldStoreAndReturnRolesClaimAndPrefix() {
        JwtProperties properties = new JwtProperties();

        properties.setRolesClaim("realm_access.roles");
        properties.setRolesPrefix("ROLE_");

        assertEquals("realm_access.roles", properties.getRolesClaim());
        assertEquals("ROLE_", properties.getRolesPrefix());
    }

    @Test
    void shouldAllowNullValuesWhenNotConfigured() {
        JwtProperties properties = new JwtProperties();

        assertNull(properties.getRolesClaim());
        assertNull(properties.getRolesPrefix());
    }
}
