package com.opsbytemitope.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SpringDoc OpenAPI 3 configuration for Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Configures OpenAPI specification with JWT security scheme.
     *
     * @return OpenAPI bean
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url(baseUrl).description("Application Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Provide the JWT token. Prefix: Bearer")));
    }

    private Info apiInfo() {
        return new Info()
                .title("OpsByTemitope API")
                .version("1.0.0")
                .description("Cloud-native task and project management platform REST API")
                .contact(new Contact()
                        .name("OpsByTemitope Engineering")
                        .email("engineering@opsbytemitope.com"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://opsbytemitope.com/license"));
    }
}
