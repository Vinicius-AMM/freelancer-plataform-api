package com.manager.freelancer_management_api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Plataforma de Gestão de Projetos Freelancers",
        version = "v1.0.0",
        description = "A plataforma permite que clientes cadastrem projetos e freelancers possam se candidatar para realizá-los. O sistema gerencia as propostas, permite negociações e registra a finalização dos trabalhos.",
        contact = @Contact(name = "Vinicius Aurélio", url = "https://www.linkedin.com/in/vinicius-amm/")))
public class OpenApiConfig {
    @Bean
    public OpenApiCustomizer removeClassFieldFromSwagger() {
        return openApi -> openApi.getComponents().getSchemas().values()
                .forEach(schema -> schema.getProperties().remove("@class"));
    }
}