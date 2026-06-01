package com.itset.itcenteamproject.domain.user.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itset.itcenteamproject.domain.survey.SurveyService;
import com.itset.itcenteamproject.domain.user.dto.LoginResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final SurveyService surveyService;

    @Override
    // 로그인 성공 시 security가 호출하는 메서드
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());

        boolean surveyCompleted = !isAdmin && surveyService.hasSurvey(user.getId());
        String redirectPath = isAdmin ? "/admin" : (surveyCompleted ? "/dashboard" : "/surveys");

        LoginResponseDTO body = LoginResponseDTO.builder()
                .loginId(user.getLoginId())
                .nickname(user.getNickname())
                .role(user.getRole())
                .isSurveyed(surveyCompleted)
                .redirectPath(redirectPath)
                .build();

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
