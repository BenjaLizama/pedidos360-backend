
package cl.pedidos360.ms_catalog.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrentUserProviderTest {

    private CurrentUserProvider provider;

    @BeforeEach
    void setUp() {
        provider = new CurrentUserProvider();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnCurrentUserIdWhenAuthenticated() {
        UUID userId = UUID.randomUUID();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userId.toString(),
                        null,
                        AuthorityUtils.createAuthorityList("ROLE_USER")
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertEquals(userId, provider.getCurrentUserId());
    }

    @Test
    void shouldThrowWhenAuthenticationIsMissing() {
        assertThrows(
                IllegalStateException.class,
                () -> provider.getCurrentUserId()
        );
    }

    @Test
    void shouldThrowWhenAuthenticationIsNotAuthenticated() {
        Authentication authentication =
                UsernamePasswordAuthenticationToken.unauthenticated(
                        "anonymous",
                        null
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThrows(
                IllegalStateException.class,
                () -> provider.getCurrentUserId()
        );
    }

    @Test
    void shouldThrowWhenAuthenticationNameIsNull() {
        Authentication authentication = new Authentication() {
            @Override
            public java.util.Collection<org.springframework.security.core.GrantedAuthority>
            getAuthorities() {
                return AuthorityUtils.createAuthorityList("ROLE_USER");
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return null;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean authenticated) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getName() {
                return null;
            }
        };

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThrows(
                IllegalStateException.class,
                () -> provider.getCurrentUserId()
        );
    }

    @Test
    void shouldThrowWhenAuthenticationNameIsNotAValidUuid() {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "not-a-uuid",
                        null,
                        AuthorityUtils.createAuthorityList("ROLE_USER")
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThrows(
                IllegalArgumentException.class,
                () -> provider.getCurrentUserId()
        );
    }
}
