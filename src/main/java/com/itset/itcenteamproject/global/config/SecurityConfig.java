package com.itset.itcenteamproject.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Locale;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 테스트 시 편리함을 위해
                //spring security 비밀번호 암호화 기능만 쓰기 위해
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())

                .authorizeHttpRequests(auth -> auth
                        // 1.Swagger 관련 리소스는 모두 허용
                        .requestMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/api/hello",
                                "/api/hello/*",
                                "/api/signup",
                                "/api/auth/**",
                                "/api/users/check",
                                "/api/me",
                                "/api/infra/**"
                        ).permitAll()
                        // 2.그 외 API는 인증 필요
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}