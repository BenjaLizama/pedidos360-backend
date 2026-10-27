package cl.pedidos360.ms_catalog.config;

import cl.pedidos360.ms_catalog.security.JwtAuthenticationConverter;
import cl.pedidos360.ms_catalog.security.JwtProperties;
import cl.pedidos360.ms_catalog.security.SecurityAccessDeniedHandler;
import cl.pedidos360.ms_catalog.security.SecurityAuthenticationEntryPoint;
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
 * Configuración de seguridad del microservicio de catálogo.
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

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final SecurityAuthenticationEntryPoint securityAuthenticationEntryPoint;
    private final SecurityAccessDeniedHandler securityAccessDeniedHandler;

    /**
     * Endpoints que pueden ser consultados sin autenticación.
     *
     * <p>Incluye endpoints de monitoreo y documentación de la API.</p>
     */
    private static final String[] WHITE_LIST_URL = {
            "/actuator/health",
            "/actuator/info",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    };

    /**
     * Endpoints públicos del catálogo.
     *
     * <p>La consulta de productos mediante HTTP GET puede realizarse
     * sin autenticación. Las operaciones de modificación permanecen
     * protegidas.</p>
     */
    private static final String[] PUBLIC_CATALOG_URL = {
            "/api/v1/products",
            "/api/v1/products/**",
            "/api/v1/categories",
            "/api/v1/categories/**"
    };

    /**
     * Construye la cadena de filtros de seguridad utilizada por Spring
     * Security.
     *
     * <p>La configuración:</p>
     * <ul>
     *     <li>Deshabilita CSRF debido a que la API es stateless.</li>
     *     <li>Deshabilita el uso de sesiones HTTP.</li>
     *     <li>Permite acceso público a los endpoints definidos.</li>
     *     <li>Requiere autenticación para cualquier otro recurso.</li>
     *     <li>Utiliza JWT como mecanismo de autenticación.</li>
     *     <li>Utiliza handlers personalizados para errores 401 y 403.</li>
     * </ul>
     *
     * @param httpSecurity configuración HTTP de Spring Security
     * @return cadena de filtros de seguridad configurada
     * @throws Exception si ocurre un error durante la construcción
     *                   de la configuración de seguridad
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
                                PUBLIC_CATALOG_URL
                        ).permitAll()

                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(securityAuthenticationEntryPoint)
                        .accessDeniedHandler(securityAccessDeniedHandler)
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter)
                        )
                );

        return httpSecurity.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
    }
}
