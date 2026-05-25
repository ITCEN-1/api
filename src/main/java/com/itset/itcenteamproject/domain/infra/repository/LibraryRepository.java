package com.itset.itcenteamproject.domain.infra.repository;

import com.itset.itcenteamproject.domain.infra.entity.Library;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LibraryRepository extends JpaRepository<Library, Long> {

    // 특정 동의 도서관 개수
    long countByDongCode(Integer dongCode);
    // 목록 조회
    List<Library> findByDongCode(Integer dongCode);

    // 여러 동의 개수를 한 번에 집계 (N+1 방지)
    @Query("select new com.itset.itcenteamproject.domain.infra.repository.DongCountDTO(l.dongCode, count(l)) " +
            "from Library l where l.dongCode in :codes group by l.dongCode")
    List<DongCountDTO> countByDongCodeIn(@Param("codes") List<Integer> dongCodes);
}
