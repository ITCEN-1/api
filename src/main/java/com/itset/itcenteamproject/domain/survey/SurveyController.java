package com.itset.itcenteamproject.domain.survey;

import com.itset.itcenteamproject.domain.survey.dto.SurveyCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/surveys")
public class SurveyController {
    private final SurveyService surveyService;

    @PostMapping
    public ResponseEntity<Long> createSurvey(@RequestBody @Valid SurveyCreateRequest request,
                                             @SessionAttribute("loginUser") Long userId) {
        Long surveyId = surveyService.createSurvey(request,userId);//생성된 질문 ID
        return ResponseEntity.status(HttpStatus.CREATED).body(surveyId);//200 대신 201응답
    }
}
