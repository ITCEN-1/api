package com.itset.itcenteamproject.domain.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder
    public User(String loginId, String password, String nickname, boolean hasSurvey) {
        this.loginId = loginId;
        this.password = password;
        this.nickname = nickname;
        this.hasSurvey = hasSurvey;
    }

    @Column(name = "login_id", unique = true, nullable = false)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(name = "survey_completed")
    private Boolean hasSurvey = false;

    @CreatedDate
    //수정해도 최초 가입시간 유지
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void completeSurvey(){
        this.hasSurvey = true;
    }
}
