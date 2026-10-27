package cl.pedidos360.ms_catalog.security;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Convierte un {@link Jwt} validado en un token de autenticación
 * compatible con Spring Security.
 *
 * <p>Extrae los roles contenidos en el JWT y los transforma en
 * {@link SimpleGrantedAuthority}, permitiendo que Spring Security
 * utilice dichos roles para aplicar las reglas de autorización.</p>
 *
 * <p>La ubicación del claim y el prefijo de las autoridades se obtienen
 * desde {@link JwtProperties}, evitando acoplar la implementación
 * directamente a un proveedor de identidad específico.</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    /**
     * Propiedades utilizadas para determinar dónde se encuentran
     * los roles dentro del JWT y qué prefijo aplicar a cada autoridad.
     */
    private final JwtProperties jwtProperties;

    /**
     * Convierte un JWT validado en un token de autenticación con
     * las autoridades correspondientes a los roles encontrados.
     *
     * <p>Por ejemplo, un rol {@code ADMIN} con el prefijo
     * {@code ROLE_} se transforma en la autoridad {@code ROLE_ADMIN}.</p>
     *
     * @param jwt token JWT previamente validado por Spring Security
     * @return token de autenticación con las autoridades extraídas
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<?> roles = extractRoles(jwt);

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(Object::toString)
                .map(role -> jwtProperties.getRolesPrefix() + role)
                .map(SimpleGrantedAuthority::new)
                .toList();

        return new JwtAuthenticationToken(jwt, authorities);
    }

    /**
     * Extrae los roles desde el claim configurado en las propiedades
     * de seguridad.
     *
     * <p>Soporta específicamente la estructura
     * {@code realm_access.roles} utilizada por Keycloak y también
     * claims que contienen directamente una colección de roles.</p>
     *
     * @param jwt token JWT desde el cual se obtendrán los roles
     * @return colección de roles encontrados; una colección vacía si
     *         el claim no existe o no contiene una colección válida
     */
    private Collection<?> extractRoles(Jwt jwt) {
        String rolesClaim = jwtProperties.getRolesClaim();

        if ("realm_access.roles".equals(rolesClaim)) {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");

            if (realmAccess == null) {
                return List.of();
            }

            Object roles = realmAccess.get("roles");

            if (roles instanceof Collection<?> collection) {
                return collection;
            }

            return List.of();
        }

        Object roles = jwt.getClaim(rolesClaim);

        if (roles instanceof Collection<?> collection) {
            return collection;
        }

        return List.of();
    }
}
