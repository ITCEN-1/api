package com.itset.itcenteamproject.domain.survey;

import com.itset.itcenteamproject.domain.survey.entity.Survey;
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
    @Query(value = "select s from Survey s left join fetch s.surveySelectedDistrictList where s.id=:surveyId")
    Optional<Survey> findSurveyById(Long surveyId);
    Optional<Survey> findTopByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Survey> findByIdAndUserId(Long id, Long userId);

    //인프라 선호도 별 응답 수 (컬럼명을 파라미터로 동적 지정할 수 없어 4종 분리)
    long countByPreferenceHospital(PreferenceLevel level);
    long countByPreferenceLargeStore(PreferenceLevel level);
    long countByPreferenceSubway(PreferenceLevel level);
    long countByPreferenceLibrary(PreferenceLevel level);
}
