package com.itset.itcenteamproject.domain.infra.repository;

import com.itset.itcenteamproject.domain.infra.entity.Library;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LibraryRepository extends JpaRepository<Library, Long> {

    // 특정 동의 도서관 개수
    long countByDongCode(Integer dongCode);
    // 목록 조회
    List<Library> findByDongCode(Integer dongCode);
}
