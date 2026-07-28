package com.smartpay.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI smartPayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartPay API")
                        .description("Production-grade Fintech Payment System")
                        .version("1.0.0"))
                .servers(List.of(
                        new Server()
                                .url("https://smartpay-production-9274.up.railway.app")
                                .description("Production"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local")
                ));
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}