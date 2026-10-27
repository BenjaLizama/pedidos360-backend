package cl.pedidos360.ms_orders.config;

import cl.pedidos360.ms_orders.security.JwtAuthenticationConverter;
import cl.pedidos360.ms_orders.security.JwtProperties;
import cl.pedidos360.ms_orders.security.SecurityAccessDeniedHandler;
import cl.pedidos360.ms_orders.security.SecurityAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad del microservicio de órdenes.
 *
 * <p>Configura la aplicación como un OAuth2 Resource Server que valida
 * tokens JWT emitidos por un proveedor de identidad externo.</p>
 *
 * <p>La aplicación utiliza una arquitectura stateless, por lo que no
 * mantiene sesiones HTTP de usuario.</p>
 *
 * <p>También define los recursos públicos, la autenticación requerida
 * para los demás endpoints y el tratamiento personalizado de errores
 * de autenticación y autorización.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    /**
     * URI del conjunto de claves JSON Web Key Set (JWKS) utilizado para
     * obtener las claves públicas con las que se verifican los tokens JWT.
     */
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    /**
     * Conversor encargado de transformar las reclamaciones del JWT
     * en las autoridades de seguridad utilizadas por Spring Security.
     */
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    /**
     * Manejador personalizado de errores de autenticación.
     *
     * <p>Se utiliza cuando una solicitud requiere autenticación válida,
     * pero no presenta credenciales válidas.</p>
     */
    private final SecurityAuthenticationEntryPoint securityAuthenticationEntryPoint;

    /**
     * Manejador personalizado de errores de autorización.
     *
     * <p>Se utiliza cuando un usuario autenticado no tiene los permisos
     * necesarios para acceder a un recurso.</p>
     */
    private final SecurityAccessDeniedHandler securityAccessDeniedHandler;

    /**
     * Endpoints que pueden consultarse sin autenticación.
     *
     * <p>Incluye los recursos de monitoreo y la documentación OpenAPI
     * de la aplicación.</p>
     */
    private static final String[] WHITE_LIST_URL = {
            "/actuator/health",
            "/actuator/info",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    };

    /**
     * Endpoints de órdenes que pueden consultarse públicamente mediante GET.
     *
     * <p>La lista se mantiene vacía en la configuración actual, por lo que
     * no se habilitan rutas de órdenes mediante esta lista para acceso público.</p>
     */
    private static final String[] PUBLIC_ORDER_URL = {

    };

    /**
     * Construye la cadena de filtros de seguridad utilizada por Spring Security.
     *
     * <p>La configuración deshabilita CSRF, establece una política de sesiones
     * stateless, permite el acceso a los endpoints incluidos en la lista blanca
     * y exige autenticación para las demás solicitudes.</p>
     *
     * <p>También configura OAuth2 Resource Server para validar tokens JWT
     * y registra los manejadores personalizados para errores de autenticación
     * y autorización.</p>
     *
     * @param httpSecurity objeto utilizado para configurar la seguridad HTTP.
     * @return cadena de filtros de seguridad configurada.
     * @throws Exception si ocurre un error al construir la configuración
     *                   de seguridad.
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(req -> req
                        .requestMatchers(WHITE_LIST_URL).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                PUBLIC_ORDER_URL
                        ).permitAll()

                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(securityAuthenticationEntryPoint)
                        .accessDeniedHandler(securityAccessDeniedHandler)
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter)
                        )
                );

        return httpSecurity.build();
    }

    /**
     * Crea el decodificador JWT utilizado por el servidor de recursos OAuth2.
     *
     * <p>El decodificador utiliza la URI JWKS configurada para obtener las
     * claves necesarias para validar la firma de los tokens.</p>
     *
     * @return decodificador JWT basado en Nimbus.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
    }
}
