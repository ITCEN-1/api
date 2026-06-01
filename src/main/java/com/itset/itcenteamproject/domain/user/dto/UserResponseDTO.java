package com.itset.itcenteamproject.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.itset.itcenteamproject.domain.user.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponseDTO {

    private Long id;
    private String loginId;
    private String nickname;
    private String role;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.loginId = user.getLoginId();
        this.nickname = user.getNickname();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt();
    }
}
