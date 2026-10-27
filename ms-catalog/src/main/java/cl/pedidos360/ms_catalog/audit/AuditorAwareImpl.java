package cl.pedidos360.ms_catalog.audit;

import cl.pedidos360.ms_catalog.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementación de {@link AuditorAware} utilizada por Spring Data MongoDB
 * para identificar al usuario responsable de una operación.
 *
 * <p>Obtiene la identidad del usuario autenticado desde el contexto de
 * seguridad de Spring Security.</p>
 *
 * <p>El identificador se obtiene mediante {@link Authentication#getName()},
 * que en el esquema de autenticación utilizado por Pedidos360 corresponde
 * al subject ({@code sub}) del JWT.</p>
 *
 * <p>El subject debe contener un UUID válido que identifique al usuario
 * dentro del sistema.</p>
 *
 * <p>Cuando no existe un usuario autenticado, la implementación devuelve
 * {@link Optional#empty()}, permitiendo que Spring Data gestione la
 * auditoría sin asociar un usuario.</p>
 */
@Component
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<UUID> {

    private final CurrentUserProvider currentUserProvider;

    /**
     * Obtiene el identificador del usuario actualmente autenticado.
     *
     * @return UUID del usuario autenticado, o {@link Optional#empty()}
     *         cuando no existe una autenticación válida
     * @throws IllegalArgumentException si el subject del JWT no contiene
     *         un UUID válido
     */
    @Override
    public Optional<UUID> getCurrentAuditor() {
        try {
            return Optional.of(
                    currentUserProvider.getCurrentUserId()
            );
        } catch (IllegalStateException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
