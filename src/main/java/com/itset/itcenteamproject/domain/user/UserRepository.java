package com.itset.itcenteamproject.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    //loginId로 회원 조회
    Optional<User> findByLoginId(String loginId);

    //아이디 중복 체크,
    boolean existsByLoginId(String loginId);

    // 특정 일자에 가입한 가입자 수
    // where createdAt = '2025-05-19' 보다 범위 비교로 해야 인덱스를 탈 수 있다고 함
    @Query("SELECT count(u) FROM User u WHERE u.createdAt >= :start AND u.createdAt < :end")
    long countByCreatedAtInDay(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

}
