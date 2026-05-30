package com.itset.itcenteamproject.domain.user.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itset.itcenteamproject.domain.user.dto.LoginRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

public class JsonLoginAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final ObjectMapper objectMapper;

    public JsonLoginAuthenticationFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        setFilterProcessesUrl("/api/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    )throws AuthenticationException{
        if (!"POST".equalsIgnoreCase(request.getMethod())){
            throw new AuthenticationServiceException("지원하지 않는 로그인 방식입니다.");
        }
        try {
            LoginRequestDTO dto = objectMapper.readValue(request.getInputStream(), LoginRequestDTO.class);

            // 아이디/비밀번호로 인증을 시도하겠다는 인증 요청 객체
            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(dto.getLoginId(), dto.getPassword());

            setDetails(request, authRequest);

            return this.getAuthenticationManager().authenticate(authRequest);
        } catch (IOException e) {
            throw new AuthenticationServiceException("로그인 요청을 읽을 수 없습니다.", e);
        }
    }
}
