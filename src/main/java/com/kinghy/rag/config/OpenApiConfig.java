package com.kinghy.rag.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI配置类
 * 使用SpringDoc 2.8.0 + Spring Boot 3.4.2 兼容版本
 * 
 * @author yunzhongxiaoma
 */
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RAG AI 知识库系统")
                        .version("1.0.0")
                        .description("基于Spring Boot和Spring AI构建的RAG知识库系统")
                        .contact(new Contact()
                                .name("yunzhongxiaoma")
                                .email("your-email@example.com")))
                .components(new Components()
                        .addSecuritySchemes("Bearer", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT认证")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer"));
    }
}