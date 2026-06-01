package com.itset.itcenteamproject.domain.infra.repository;

import com.itset.itcenteamproject.domain.infra.entity.DongLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DongLocationRepository extends JpaRepository<DongLocation, Integer> {

    // 후보 법정동 코드들에 해당하는 동 정보 조회
    List<DongLocation> findByDongCodeIn(List<Integer> dongCodes);
    DongLocation getDongLocationByDongCode(Integer dongCode);
    List<DongLocation> findAllByDistrictName(String districtName);
    //구 중복 제거 목록 조회
    //구 선택 드롭다운을 DB 기반으로 자동 생성하기 위해
    @Query("select distinct d.districtName from DongLocation d order by d.districtName asc")
    List<String> findAllDistrictNames();
}
