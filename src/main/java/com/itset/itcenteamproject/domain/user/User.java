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
    public User(String loginId, String password, String nickname) {
        this.loginId = loginId;
        this.password = password;
        this.nickname = nickname;
        this.role = "USER";  // 회원가입으로 생성되는 계정은 기본 일반 사용자
    }

    @Column(name = "login_id", unique = true, nullable = false)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String nickname;

    //관리자 확인
    @Column(nullable = false)
    private String role;

    @CreatedDate
    //수정해도 최초 가입시간 유지
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public boolean isAdmin() {
        return "ADMIN".equals(this.role);
    }

}
