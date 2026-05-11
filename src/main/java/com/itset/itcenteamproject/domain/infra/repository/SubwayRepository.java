package com.itset.itcenteamproject.domain.infra.repository;

import com.itset.itcenteamproject.domain.infra.entity.Subway;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubwayRepository extends JpaRepository<Subway, Long> {

    // 특정 동의 지하철 개수
    long countByDongCode(Integer dongCode);
    // 목록 조회
    List<Subway> findByDongCode(Integer dongCode);
}
