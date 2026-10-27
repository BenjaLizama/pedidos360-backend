package cl.pedidos360.ms_orders.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationConverterTest {

    @Mock
    private JwtProperties jwtProperties;

    private JwtAuthenticationConverter converter;

    @BeforeEach
    void setUp() {
        converter = new JwtAuthenticationConverter(jwtProperties);
    }

    @Test
    void convert_whenRealmAccessContainsRoles_extractsAuthorities() {
        when(jwtProperties.getRolesClaim())
                .thenReturn("realm_access.roles");
        when(jwtProperties.getRolesPrefix())
                .thenReturn("ROLE_");

        Jwt jwt = jwtWithClaims(Map.of(
                "realm_access",
                Map.of("roles", List.of("ADMINISTRADOR", "OPERADOR"))
        ));

        JwtAuthenticationToken result =
                (JwtAuthenticationToken) converter.convert(jwt);

        assertNotNull(result);
        assertEquals(2, result.getAuthorities().size());
        assertTrue(result.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")));
        assertTrue(result.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_OPERADOR")));
    }

    @Test
    void convert_whenRolesClaimIsNull_usesDefaultClaim() {
        when(jwtProperties.getRolesClaim()).thenReturn(null);
        when(jwtProperties.getRolesPrefix()).thenReturn(null);

        Jwt jwt = jwtWithClaims(Map.of(
                "realm_access",
                Map.of("roles", List.of("ADMINISTRADOR"))
        ));

        JwtAuthenticationToken result =
                (JwtAuthenticationToken) converter.convert(jwt);

        assertEquals(
                List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")),
                result.getAuthorities().stream().toList()
        );
    }

    @Test
    void convert_whenRolesClaimIsBlank_usesDefaultClaim() {
        when(jwtProperties.getRolesClaim()).thenReturn("   ");
        when(jwtProperties.getRolesPrefix()).thenReturn("ROLE_");

        Jwt jwt = jwtWithClaims(Map.of(
                "realm_access",
                Map.of("roles", List.of("OPERADOR"))
        ));

        JwtAuthenticationToken result =
                (JwtAuthenticationToken) converter.convert(jwt);

        assertTrue(result.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_OPERADOR")));
    }

    @Test
    void convert_whenCustomClaimContainsRoles_extractsAuthorities() {
        when(jwtProperties.getRolesClaim()).thenReturn("roles");
        when(jwtProperties.getRolesPrefix()).thenReturn("ROLE_");

        Jwt jwt = jwtWithClaims(Map.of(
                "roles", List.of("ADMINISTRADOR", "OPERADOR")
        ));

        JwtAuthenticationToken result =
                (JwtAuthenticationToken) converter.convert(jwt);

        assertEquals(2, result.getAuthorities().size());
        assertTrue(result.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")));
        assertTrue(result.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_OPERADOR")));
    }

    @Test
    void convert_whenCustomPrefixIsConfigured_appliesPrefix() {
        when(jwtProperties.getRolesClaim()).thenReturn("roles");
        when(jwtProperties.getRolesPrefix()).thenReturn("PERM_");

        Jwt jwt = jwtWithClaims(Map.of(
                "roles", List.of("ADMINISTRADOR")
        ));

        JwtAuthenticationToken result =
                (JwtAuthenticationToken) converter.convert(jwt);

        assertTrue(result.getAuthorities().contains(
                new SimpleGrantedAuthority("PERM_ADMINISTRADOR")));
    }

    @Test
    void convert_whenRealmAccessIsMissing_returnsNoAuthorities() {
        when(jwtProperties.getRolesClaim())
                .thenReturn("realm_access.roles");
        when(jwtProperties.getRolesPrefix()).thenReturn("ROLE_");

        Jwt jwt = jwtWithClaims(Map.of());

        JwtAuthenticationToken result =
                (JwtAuthenticationToken) converter.convert(jwt);

        assertTrue(result.getAuthorities().isEmpty());
    }

    @Test
    void convert_whenRealmRolesAreNotACollection_returnsNoAuthorities() {
        when(jwtProperties.getRolesClaim())
                .thenReturn("realm_access.roles");
        when(jwtProperties.getRolesPrefix()).thenReturn("ROLE_");

        Jwt jwt = jwtWithClaims(Map.of(
                "realm_access",
                Map.of("roles", "ADMINISTRADOR")
        ));

        JwtAuthenticationToken result =
                (JwtAuthenticationToken) converter.convert(jwt);

        assertTrue(result.getAuthorities().isEmpty());
    }

    @Test
    void convert_whenCustomClaimIsNotACollection_returnsNoAuthorities() {
        when(jwtProperties.getRolesClaim()).thenReturn("roles");
        when(jwtProperties.getRolesPrefix()).thenReturn("ROLE_");

        Jwt jwt = jwtWithClaims(Map.of(
                "roles", "ADMINISTRADOR"
        ));

        JwtAuthenticationToken result =
                (JwtAuthenticationToken) converter.convert(jwt);

        assertTrue(result.getAuthorities().isEmpty());
    }

    @Test
    void convert_whenCustomClaimIsMissing_returnsNoAuthorities() {
        when(jwtProperties.getRolesClaim()).thenReturn("custom_roles");
        when(jwtProperties.getRolesPrefix()).thenReturn("ROLE_");

        Jwt jwt = jwtWithClaims(Map.of());

        JwtAuthenticationToken result =
                (JwtAuthenticationToken) converter.convert(jwt);

        assertTrue(result.getAuthorities().isEmpty());
    }

    private Jwt jwtWithClaims(Map<String, Object> claims) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("test-user")
                .claim("test_marker", true)
                .claims(existingClaims -> existingClaims.putAll(claims))
                .build();
    }
}
