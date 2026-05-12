package com.itset.itcenteamproject.domain.infra.repository;

import com.itset.itcenteamproject.domain.infra.entity.LargeStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LargeStoreRepository extends JpaRepository<LargeStore, Long> {

    // 특정 동의 대규모점포 개수
    long countByDongCode(Integer dongCode);
    // 목록 조회
    List<LargeStore> findByDongCode(Integer dongCode);
}
