package com.grupo81.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Value("${springdoc.swagger-ui.oauth.authorization-url:${springdoc.oauth2.authorization-url}}")
    private String authorizationUrl;
    
    @Value("${springdoc.swagger-ui.oauth.token-url:${springdoc.oauth2.token-url}}")
    private String tokenUrl;
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API Servicio Tarifa")
                .version("1.0.0")
                .description("API REST para la gestión de tarifas"))
            .addSecurityItem(new SecurityRequirement().addList("oauth2"))
            .components(new Components()
                .addSecuritySchemes("oauth2", new SecurityScheme()
                    .type(SecurityScheme.Type.OAUTH2)
                    .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                            .authorizationUrl(authorizationUrl)
                            .tokenUrl(tokenUrl)
                            .scopes(new Scopes())))));
    }
}