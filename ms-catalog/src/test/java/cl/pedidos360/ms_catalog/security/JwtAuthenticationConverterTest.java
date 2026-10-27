
package cl.pedidos360.ms_catalog.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationConverterTest {

    private JwtProperties properties;
    private JwtAuthenticationConverter converter;

    @BeforeEach
    void setUp() {
        properties = new JwtProperties();
        properties.setRolesClaim("realm_access.roles");
        properties.setRolesPrefix("ROLE_");

        converter = new JwtAuthenticationConverter(properties);
    }

    @Test
    void shouldConvertRolesFromRealmAccess() {
        Jwt jwt = createJwt(
                Map.of("realm_access", Map.of(
                        "roles", List.of("ADMIN", "STAFF")
                ))
        );

        JwtAuthenticationToken result =
                (JwtAuthenticationToken) converter.convert(jwt);

        assertNotNull(result);
        assertTrue(result.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        ));
        assertTrue(result.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_STAFF")
        ));
        assertEquals(2, result.getAuthorities().size());
    }

    @Test
    void shouldConvertRolesFromDirectClaim() {
        properties.setRolesClaim("roles");

        JwtAuthenticationConverter directConverter =
                new JwtAuthenticationConverter(properties);

        Jwt jwt = createJwt(Map.of(
                "roles", List.of("ADMIN", "CUSTOMER")
        ));

        JwtAuthenticationToken result =
                (JwtAuthenticationToken) directConverter.convert(jwt);

        assertEquals(2, result.getAuthorities().size());
        assertTrue(result.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        ));
        assertTrue(result.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_CUSTOMER")
        ));
    }

    @Test
    void shouldReturnNoAuthoritiesWhenRealmAccessIsMissing() {
        Jwt jwt = createJwt(Map.of());

        JwtAuthenticationToken result =
                (JwtAuthenticationToken) converter.convert(jwt);

        assertTrue(result.getAuthorities().isEmpty());
    }

    @Test
    void shouldReturnNoAuthoritiesWhenRealmRolesAreNotACollection() {
        Jwt jwt = createJwt(Map.of(
                "realm_access", Map.of("roles", "ADMIN")
        ));

        JwtAuthenticationToken result =
                (JwtAuthenticationToken) converter.convert(jwt);

        assertTrue(result.getAuthorities().isEmpty());
    }

    @Test
    void shouldReturnNoAuthoritiesWhenDirectClaimIsMissing() {
        properties.setRolesClaim("roles");

        JwtAuthenticationConverter directConverter =
                new JwtAuthenticationConverter(properties);

        Jwt jwt = createJwt(Map.of());

        JwtAuthenticationToken result =
                (JwtAuthenticationToken) directConverter.convert(jwt);

        assertTrue(result.getAuthorities().isEmpty());
    }

    @Test
    void shouldApplyConfiguredPrefix() {
        properties.setRolesPrefix("");

        Jwt jwt = createJwt(Map.of(
                "realm_access", Map.of("roles", List.of("ADMIN"))
        ));

        JwtAuthenticationToken result =
                (JwtAuthenticationToken) converter.convert(jwt);

        assertTrue(result.getAuthorities().contains(
                new SimpleGrantedAuthority("ADMIN")
        ));
    }

    private Jwt createJwt(Map<String, Object> claims) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("user-test")
                .claims(jwtClaims -> jwtClaims.putAll(claims))
                .build();
    }
}
