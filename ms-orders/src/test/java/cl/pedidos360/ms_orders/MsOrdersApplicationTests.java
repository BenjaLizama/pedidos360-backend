package cl.pedidos360.ms_orders;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(
		properties = {
				"spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/realms/pedidos360/protocol/openid-connect/certs"
		}
)
class MsOrdersApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void contextLoads() {
		assertNotNull(applicationContext);
	}
}
