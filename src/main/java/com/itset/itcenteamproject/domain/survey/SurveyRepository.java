package com.itset.itcenteamproject.domain.survey;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    boolean existsByUserId(Long userId);
    @Query(value = "select s from Survey s left join fetch s.surveySelectedDistrictList where s.user.id = :userId",
          countQuery = "select count(s) from Survey s where s.user.id = :userId")
    Page<Survey> findSurveyByUserId(Long userId, Pageable pageable);
    Optional<Survey> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
