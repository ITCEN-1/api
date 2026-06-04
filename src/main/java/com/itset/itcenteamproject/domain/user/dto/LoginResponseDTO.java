package com.itset.itcenteamproject.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LoginResponseDTO {
    private String loginId;
    private String nickname;
    private String role;
    private Boolean isSurveyed;
    private String redirectPath;
}

