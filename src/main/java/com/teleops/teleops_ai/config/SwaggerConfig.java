package com.teleops.teleops_ai.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 3.0 Configuration
 *
 * This configures the auto-generated API documentation.
 *
 * Key configuration here:
 *   We register a "Bearer Authentication" security scheme.
 *   This adds an "Authorize" button to Swagger UI where
 *   engineers can paste their JWT token and test
 *   protected endpoints directly from the browser.
 *
 * Access Swagger UI at: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI teleopsOpenAPI() {

        // Define the JWT Bearer security scheme
        SecurityScheme securityScheme = new SecurityScheme()
                .name("Bearer Authentication")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter your JWT token. " +
                        "Get it from POST /api/v1/auth/login");

        // Apply security globally to all endpoints
        SecurityRequirement securityRequirement =
                new SecurityRequirement()
                        .addList("Bearer Authentication");

        return new OpenAPI()
                .info(new Info()
                        .title("TeleOps AI API")
                        .description("AI-Powered Telecom Operations Platform API. " +
                                "Manages 4G/5G towers, routers, firewalls, switches " +
                                "and base stations with AI-driven incident management.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("TeleOps Engineering Team")
                                .email("engineering@teleops.com")))
                .addSecurityItem(securityRequirement)
                .components(new Components()
                        .addSecuritySchemes(
                                "Bearer Authentication", securityScheme));
    }
}