package com.itset.itcenteamproject.domain.infra.repository;

import com.itset.itcenteamproject.domain.infra.entity.LargeStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LargeStoreRepository extends JpaRepository<LargeStore, Long> {

    // 특정 동의 대규모점포 개수
    long countByDongCode(Integer dongCode);
    // 목록 조회
    List<LargeStore> findByDongCode(Integer dongCode);

    // 여러 동의 개수를 한 번에 집계 (N+1 방지)
    @Query("select new com.itset.itcenteamproject.domain.infra.repository.DongCountDTO(ls.dongCode, count(ls)) " +
            "from LargeStore ls where ls.dongCode in :codes group by ls.dongCode")
    List<DongCountDTO> countByDongCodeIn(@Param("codes") List<Integer> dongCodes);
}
