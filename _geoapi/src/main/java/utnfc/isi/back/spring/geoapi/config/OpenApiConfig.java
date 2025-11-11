package utnfc.isi.back.spring.geoapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("GeoAPI - Servicio de Distancias")
                .version("1.0.0")
                .description("API para calcular distancias y tiempos usando Google Maps Distance Matrix API")
                .contact(new Contact()
                    .name("Grupo 81")
                    .email("grupo81@example.com")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8085")
                    .description("Servidor Local")
            ));
    }
}