package com.itset.itcenteamproject.domain.survey;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    //테스트용
    //설문 개수가 아니라 설문 여부를 확인하는 것이기 때문에 count안 쓰고 exists로 사용함
    boolean existsByUserId(Long userId);
    @Query(value = "select s from Survey s left join fetch s.surveySelectedDistrictList where s.user.id = :userId",
            countQuery = "select count(s) from Survey s where s.user.id = :userId")
    Page<Survey> findSurveyByUserId(Long userId, Pageable pageable);
}
