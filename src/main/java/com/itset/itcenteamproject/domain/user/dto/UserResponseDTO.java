package com.itset.itcenteamproject.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.itset.itcenteamproject.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponseDTO {

    private Long id;
    private String loginId;
    private String nickname;
    private String role;
    private Boolean isSurveyed;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
