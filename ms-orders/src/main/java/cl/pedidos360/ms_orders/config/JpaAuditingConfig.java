package cl.pedidos360.ms_orders.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuración de auditoría para las entidades JPA del microservicio ms-orders.
 *
 * <p>Habilita el mecanismo de auditoría de Spring Data JPA, permitiendo
 * registrar automáticamente información relacionada con la creación y
 * modificación de las entidades que utilizan las anotaciones de auditoría.</p>
 *
 * <p>El componente {@code auditorAwareImpl} proporciona la información del
 * usuario autenticado que se utilizará para completar los campos de auditoría.</p>
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
public class JpaAuditingConfig {

}
