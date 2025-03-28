package infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
		"application",  // Casos de uso y lógica de negocio
		"domain",       // Modelos y entidades
		"infrastructure", // Configuración, seguridad, repositorios
		"web"           // Controladores (Endpoints API REST)
})
@EnableJpaRepositories(basePackages = "infrastructure.repositories") // Ajusta según la ubicación exacta
@EntityScan(basePackages = "domain") // Para detectar las entidades
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
