package cl.pedidos360.ms_orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class MsOrdersApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsOrdersApplication.class, args);
	}

}
