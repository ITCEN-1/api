package com.itset.itcenteamproject.domain.survey;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface SurveyRepository extends JpaRepository<Survey, Long> {

    boolean existsByUserId(Long userId);

    Optional<Survey> findByIdAndUserId(Long id, Long userId);
}
