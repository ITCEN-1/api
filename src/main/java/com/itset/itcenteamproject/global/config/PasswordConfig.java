package com.itset.itcenteamproject.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    //Spring container에 등록.
    //어디서든 주입 가능.
    public BCryptPasswordEncoder passwordEncoder() {
        //비밀번호 암호화(단방향 해시 암호화 라이브러리)
        return new BCryptPasswordEncoder();
    }
}