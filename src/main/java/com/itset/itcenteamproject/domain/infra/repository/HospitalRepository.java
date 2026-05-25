package com.itset.itcenteamproject.domain.infra.repository;

import com.itset.itcenteamproject.domain.infra.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    // 특정 동의 병원 개수
    long countByDongCode(Integer dongCode);
    // 목록 조회
    List<Hospital> findByDongCode(Integer dongCode);

    // 여러 동의 개수를 한 번에 집계 (N+1 방지)
    @Query("select new com.itset.itcenteamproject.domain.infra.repository.DongCountDTO(h.dongCode, count(h)) " +
            "from Hospital h where h.dongCode in :codes group by h.dongCode")
    List<DongCountDTO> countByDongCodeIn(@Param("codes") List<Integer> dongCodes);
}
