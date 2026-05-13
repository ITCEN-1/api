package com.itset.itcenteamproject.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("Backend API")
                .description("백엔드 API 명세서입니다.")
                .version("v1.0.0")
                .contact(new Contact().name("Kim_Joon_hyuk").email("joonlife0901@gmail.com"));

        // 서버 환경 설정
        Server localServer = new Server().url("http://localhost:8080").description("Local Server");

        Server productionServer = new Server()
                .url("http://35.203.175.24:8080")
                .description("Production Server");

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer, productionServer));
    }
}