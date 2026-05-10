package com.itset.itcenteamproject.domain.infra.repository;

import com.itset.itcenteamproject.domain.infra.entity.DongLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DongLocationRepository extends JpaRepository<DongLocation, Integer> {

    // 후보 법정동 코드들에 해당하는 동 정보 조회
    List<DongLocation> findByDongCodeIn(List<Integer> dongCodes);
}
