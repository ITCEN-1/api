package com.itset.itcenteamproject.domain.user.service;

import com.itset.itcenteamproject.domain.user.security.CustomUserDetails;
import com.itset.itcenteamproject.exception.CustomException;
import com.itset.itcenteamproject.exception.ErrorCode;
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
}