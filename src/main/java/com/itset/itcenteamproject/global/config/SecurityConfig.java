package com.itset.itcenteamproject.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itset.itcenteamproject.domain.user.security.JsonLoginAuthenticationFilter;
import com.itset.itcenteamproject.domain.user.security.LoginFailureHandler;
import com.itset.itcenteamproject.domain.user.security.LoginSuccessHandler;
import com.itset.itcenteamproject.exception.ErrorCode;
import com.itset.itcenteamproject.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository)
            throws Exception {
        JsonLoginAuthenticationFilter jsonLoginFilter = new JsonLoginAuthenticationFilter(objectMapper);
        jsonLoginFilter.setAuthenticationManager(authenticationManager);
        jsonLoginFilter.setSecurityContextRepository(securityContextRepository);
        jsonLoginFilter.setAuthenticationSuccessHandler(loginSuccessHandler);
        jsonLoginFilter.setAuthenticationFailureHandler(loginFailureHandler);

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable()) // 테스트 시 편리함을 위해
                // 브라우저 기본 인증창도 안 쓰고, Spring 기본 form 로그인 안 씀
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                //.logout(logout -> logout.disable())
                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository)
                )

                .authorizeHttpRequests(auth -> auth
                        // 1.Swagger 관련 리소스는 모두 허용
                        .requestMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/api/hello",
                                "/api/hello/*",
                                "/auth/**",
//                                "/communities/**",
                                "/api/auth/**",
                                "/api/signup",
                                "/api/users/check",
                                "/api/users/check-nickname"
                                /*,
                                "/api/surveys/**",
                                "/api/infra/**",
                                "/api/dashboard/**",
                                "/api/history/**",
                                "/api/dashboards/test",
                                "/api/dashboards/test"*/
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 2.그 외 API는 인증 필요
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            String uri = request.getRequestURI();

                            if (uri.startsWith("/communities")) {
                                response.sendRedirect("http://localhost:5173/login");
                                return;
                            }

                            response.setStatus(ErrorCode.SESSION_EXPIRED.getStatus().value());
                            response.setContentType("application/json;charset=UTF-8");
                            objectMapper.writeValue(response.getWriter(), ApiResponse.error(ErrorCode.SESSION_EXPIRED));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            objectMapper.writeValue(response.getWriter(), ApiResponse.error(ErrorCode.BAD_REQUEST));
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(204);
                        })
                )
                // 기본 form 로그인 필터 대신 우리 필터가 /api/auth/login 처리
                .addFilterAt(jsonLoginFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}