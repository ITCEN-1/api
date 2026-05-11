package com.itset.itcenteamproject.domain.infra.repository;

import com.itset.itcenteamproject.domain.infra.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    // 특정 동의 병원 개수
    long countByDongCode(Integer dongCode);
}
