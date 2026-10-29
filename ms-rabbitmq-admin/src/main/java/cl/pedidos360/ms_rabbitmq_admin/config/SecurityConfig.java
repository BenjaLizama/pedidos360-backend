package cl.pedidos360.ms_rabbitmq_admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Value("${security.jwt.roles-claim:realm_access.roles}")
  private String rolesClaim;

  @Value("${security.jwt.roles-prefix:ROLE_}")
  private String rolesPrefix;

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    Converter<Jwt, AbstractAuthenticationToken> converter = jwt -> {
      Object rawRoles;
      if ("realm_access.roles".equals(rolesClaim)) {
        Map<String, Object> realm = jwt.getClaim("realm_access");
        rawRoles = realm == null ? null : realm.get("roles");
      } else {
        rawRoles = jwt.getClaim(rolesClaim);
      }

      Collection<?> roles = rawRoles instanceof Collection<?> values ? values : List.of();
      List<SimpleGrantedAuthority> authorities = roles.stream()
          .map(Object::toString)
          .map(role -> new SimpleGrantedAuthority(rolesPrefix + role))
          .toList();
      return new JwtAuthenticationToken(jwt, authorities);
    };

    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)));
    return http.build();
  }
}