package com.itset.itcenteamproject.domain.infra.repository;

import com.itset.itcenteamproject.domain.infra.entity.LargeStore;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LargeStoreRepository extends JpaRepository<LargeStore, Long> {

    // 특정 동의 대규모점포 개수
    long countByDongCode(Integer dongCode);
}
