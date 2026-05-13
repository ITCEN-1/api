package com.itset.itcenteamproject.domain.user.service;

import com.itset.itcenteamproject.exception.CustomException;
import com.itset.itcenteamproject.exception.ErrorCode;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
//세션 관련 판단
public class SessionUserService {
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

    //로그아웃 세션
    public void logout(HttpSession session) {
        getLoginUserId(session);
        session.invalidate();
    }
}
