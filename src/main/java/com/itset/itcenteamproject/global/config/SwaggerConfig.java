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
        /*
        보안 관련 설정 필요할때만 사용

        // 1. 보안 스키마 이름 정의 (원하는 이름으로 설정 가능)
        String jwtSchemeName = "jwtAuth";

        // 2. API 요청 시 보안 요구사항 추가 (전역 설정)
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        // 3. Components에 보안 스키마 등록 (JWT Bearer 방식)
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP) // HTTP 방식
                        .scheme("bearer")              // bearer 인증
                        .bearerFormat("JWT")           // 문서에 JWT임을 명시
                        .in(SecurityScheme.In.HEADER)  // 헤더에 담아 전송
                        .description("JSON Web Token (JWT)을 입력해주세요."));

         */

        // 4. API 정보 및 서버 환경 설정
        Info info = new Info()
                .title("Backend API")
                .description("백엔드 API 명세서입니다.")
                .version("v1.0.0")
                .contact(new Contact().name("Kim_Joon_hyuk").email("joonlife0901@gmail.com"));

        // 서버 환경 설정
        Server localServer = new Server().url("http://localhost:8080").description("Local Server");

        /*return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement) // 모든 API에 자물쇠 아이콘 표시
                .components(components)
                .servers(List.of(localServer));*/

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
}