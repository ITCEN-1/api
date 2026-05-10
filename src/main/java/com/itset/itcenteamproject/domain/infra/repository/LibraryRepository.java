package com.itset.itcenteamproject.domain.infra.repository;

import com.itset.itcenteamproject.domain.infra.entity.Library;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryRepository extends JpaRepository<Library, Long> {

    // 특정 동의 도서관 개수
    long countByDongCode(Integer dongCode);
}
