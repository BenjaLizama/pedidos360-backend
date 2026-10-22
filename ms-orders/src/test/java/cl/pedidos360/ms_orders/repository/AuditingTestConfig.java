package cl.pedidos360.ms_orders.repository;

import cl.pedidos360.ms_orders.audit.AuditorAwareImpl;
import cl.pedidos360.ms_orders.security.CurrentUserProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class AuditingTestConfig {

    @Bean("auditorAwareImpl")
    public AuditorAwareImpl auditorAwareImpl(
            CurrentUserProvider currentUserProvider
    ) {
        return new AuditorAwareImpl(currentUserProvider);
    }
}
