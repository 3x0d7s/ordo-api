package com.kyut.ordo.core.openApi.config;

import com.kyut.ordo.core.openApi.PageableOpenApiCustomizer;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(PageableOpenApiCustomizer pageableOpenApiCustomizer) {
        OpenAPI openAPI = new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .info(new Info().title("Ordo").version("0.0.1"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));

        pageableOpenApiCustomizer.customise(openAPI);

        return openAPI;
    }

}
