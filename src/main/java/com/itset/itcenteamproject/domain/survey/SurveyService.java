package com.itset.itcenteamproject.domain.survey;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SurveyService {
    private final SurveyRepository surveyRepository;

    //설문여부 확인
    public boolean hasSurvey(Long userId) {
        return surveyRepository.existsByUserId(userId);
    }
}
