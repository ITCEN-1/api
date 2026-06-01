package com.itset.itcenteamproject.domain.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;

import java.util.List;



public interface HistoryItemRepository extends JpaRepository<HistoryItem,Long> {

    public
    //가장 많이 추천된 상위 10개 동
    @Query("SELECT hi.dongCode, COUNT(hi) "+
            "FROM HistoryItem hi "+
            "GROUP BY hi.dongCode "+
            "ORDER BY COUNT(hi) DESC")
    List<DongCodeCountDto> findTop10DongCode(Pageable pageable);
}
