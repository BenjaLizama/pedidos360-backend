package cl.pedidos360.ms_catalog.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Proveedor centralizado del usuario autenticado actualmente.
 *
 * <p>El identificador del usuario se obtiene desde el subject
 * del JWT autenticado.</p>
 */
@Component
public class CurrentUserProvider {

    /**
     * Obtiene el identificador UUID del usuario autenticado.
     *
     * @return UUID del usuario autenticado
     * @throws IllegalStateException si no existe un usuario autenticado
     */
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw new IllegalStateException("No existe un usuario autenticado.");
        }

        return UUID.fromString(authentication.getName());
    }
}
