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
                .title("DongnePick API")
                .description("아이티센 1차 프로젝트 동네픽 API 명세서")
                .version("v1.0.1")
                .contact(new Contact().name("Kim_Joon_hyuk").email("joonlife0901@gmail.com"));

        // 서버 환경 설정
        Server localServer = new Server().url("http://localhost:8080").description("Local Server");

        Server productionServer = new Server()
                .url("http://35.203.175.24:8080")
                .description("Production Server");

        // 쿠키 기반 세션 인증 추가
        SecurityScheme cookieScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("JSESSIONID");

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer, productionServer))
                .addSecurityItem(new SecurityRequirement().addList("cookieAuth"))
                .components(new Components()
                        .addSecuritySchemes("cookieAuth", cookieScheme));
    }
}