package com.itset.itcenteamproject.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDTO {

    private String loginId;
    private String nickname;
    //private Boolean hasSurvey;
}
