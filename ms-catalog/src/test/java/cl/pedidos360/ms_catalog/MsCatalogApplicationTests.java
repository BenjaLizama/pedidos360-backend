package cl.pedidos360.ms_catalog;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(
		properties = {
				"spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/realms/pedidos360/protocol/openid-connect/certs"
		}
)
class MsCatalogApplicationTests {

	@Test
	void contextLoads() {
	}
}
