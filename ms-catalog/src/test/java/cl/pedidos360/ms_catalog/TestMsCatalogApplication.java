package cl.pedidos360.ms_catalog;

import org.springframework.boot.SpringApplication;

public class TestMsCatalogApplication {

	public static void main(String[] args) {
		SpringApplication.from(MsCatalogApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
