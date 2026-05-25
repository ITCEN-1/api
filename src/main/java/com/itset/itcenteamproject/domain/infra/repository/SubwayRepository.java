package com.itset.itcenteamproject.domain.infra.repository;

import com.itset.itcenteamproject.domain.infra.entity.Subway;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubwayRepository extends JpaRepository<Subway, Long> {

    // 특정 동의 지하철 개수
    long countByDongCode(Integer dongCode);
    // 목록 조회
    List<Subway> findByDongCode(Integer dongCode);

    // 여러 동의 개수를 한 번에 집계 (N+1 방지)
    @Query("select new com.itset.itcenteamproject.domain.infra.repository.DongCountDTO(s.dongCode, count(s)) " +
            "from Subway s where s.dongCode in :codes group by s.dongCode")
    List<DongCountDTO> countByDongCodeIn(@Param("codes") List<Integer> dongCodes);
}
