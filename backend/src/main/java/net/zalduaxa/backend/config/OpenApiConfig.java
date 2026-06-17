package net.zalduaxa.backend.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI zalduaxaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Zalduaxa.net API")
                        .description("""
                                REST API for Zalduaxa.net.

                                This documentation is generated automatically from the Spring Boot backend.
                                It is intended for local development, debugging, API review and project documentation.
                                """)
                        .version("v1")
                        .contact(new Contact()
                                .name("Zalduaxa.net")
                                .url("https://zalduaxa.net"))
                        .license(new License()
                                .name("Private project")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Docker backend"),
                        new Server()
                                .url("http://backend:8080")
                                .description("Internal Docker network backend")
                ))
                .externalDocs(new ExternalDocumentation()
                        .description("Zalduaxa.net")
                        .url("https://zalduaxa.net"));
    }
}