package com.itset.itcenteamproject.domain.user;

import com.itset.itcenteamproject.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    //loginId로 회원 조회
    Optional<User> findByLoginId(String loginId);

    //아이디 중복 체크,
    boolean existsByLoginId(String loginId);

    //닉네임 중복 체크
    //boolean existsByNickname(String nickname);
}
