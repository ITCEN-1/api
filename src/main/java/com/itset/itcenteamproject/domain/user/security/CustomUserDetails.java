package com.itset.itcenteamproject.domain.user.security;

import com.itset.itcenteamproject.domain.user.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String loginId;
    private final String password;
    private final String nickname;
    private final String role;

    // DB에서 조회한 User 엔티티를 Security용 객체로 바꿔준다.
    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.loginId = user.getLoginId();
        this.password = user.getPassword();
        this.nickname = user.getNickname();
        this.role = user.getRole();
    }

    // 로그인 후 이 메서드를 통해 사용자의 권한을 확인
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return loginId;
    }
}