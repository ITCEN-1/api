package com.itset.itcenteamproject.domain.user.service;

import com.itset.itcenteamproject.domain.user.security.CustomUserDetails;
import com.itset.itcenteamproject.exception.CustomException;
import com.itset.itcenteamproject.exception.ErrorCode;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SessionUserService {

    public Long getLoginUserId() {
        return getLoginUser().getId();
    }

    public CustomUserDetails getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                // authentication.getPrincipal()이 진짜 우리가 만든 CustomUserDetails 타입인지 확인
                || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new CustomException(ErrorCode.SESSION_EXPIRED);
        }

        return userDetails;
    }

    //로그인 테스트용
    private static final String LOGIN_USER = "loginUser";

    //인증 체크(세션에 loginUser가 있는지)
    public Long getLoginUserId(HttpSession session) {
        //로그인 상태 -> userid 반환
        Long userId = (Long) session.getAttribute(LOGIN_USER);
        //세션 없음 -> 401예외
        if (userId == null) {
            throw new CustomException(ErrorCode.SESSION_EXPIRED);
        }
        return userId;
    }

    //로그인 세션
    public void login(HttpSession session, Long userId) {
        session.setAttribute(LOGIN_USER, userId);
        session.setMaxInactiveInterval(18000);
    }
}