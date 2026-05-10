package com.itset.itcenteamproject.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDTO {
    private String loginId;
    private String password;
    private String nickname;
}
