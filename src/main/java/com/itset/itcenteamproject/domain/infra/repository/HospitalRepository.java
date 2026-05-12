package com.itset.itcenteamproject.domain.infra.repository;

import com.itset.itcenteamproject.domain.infra.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    // 특정 동의 병원 개수
    long countByDongCode(Integer dongCode);
    // 목록 조회
    List<Hospital> findByDongCode(Integer dongCode);
}
