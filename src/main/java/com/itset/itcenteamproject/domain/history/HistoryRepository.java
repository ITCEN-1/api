package com.itset.itcenteamproject.domain.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HistoryRepository extends JpaRepository<History, Long> {
    @Query("select h from History as h" +
            " left join fetch h.historyItems" +
            " where h.survey.id = :surveyId")
    History findHistoriesBySurveyId(Long surveyId);
    boolean existsBySurveyId(Long surveyId);
}
