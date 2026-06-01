package com.itset.itcenteamproject.domain.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface HistoryRepository extends JpaRepository<History, Long> {
    @Query("select h from History as h" +
            " left join fetch h.historyItems as hi" +
            " left join fetch hi.dongLocation" +
            " where h.survey.id = :surveyId")
    Optional<History> findHistoriesBySurveyId(Long surveyId);
    @Query("select h from History as h" +
            " left join fetch h.historyItems as hi" +
            " left join fetch hi.dongLocation" +
            " where h.user.id = :userId " +
            "order by h.createdAt desc " +
            "limit 1")
    Optional<History> findHistoriesByLatest(Long userId);
    boolean existsBySurveyId(Long surveyId);
}
