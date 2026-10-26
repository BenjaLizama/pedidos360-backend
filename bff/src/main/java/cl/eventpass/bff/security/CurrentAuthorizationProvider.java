
package cl.eventpass.bff.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Proporciona el encabezado Authorization del usuario autenticado
 * para las llamadas del BFF hacia los microservicios.
 */
@Component
public class CurrentAuthorizationProvider {

    /**
     * Obtiene el encabezado Authorization de la autenticación actual.
     *
     * @return token con prefijo Bearer, o null si la solicitud es anónima.
     */
    public String getAuthorization() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)
                || !authentication.isAuthenticated()) {
            return null;
        }

        return "Bearer " + jwtAuthentication.getToken().getTokenValue();
    }
}
