package com.itset.itcenteamproject.domain.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DongLocationRepository extends JpaRepository<DongLocation,Integer> {

    List<DongLocation> findAllByDistrictName(String districtName);
}
