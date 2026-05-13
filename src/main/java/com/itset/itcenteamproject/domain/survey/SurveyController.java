package com.itset.itcenteamproject.domain.survey;

import com.itset.itcenteamproject.domain.survey.dto.SurveyCreateRequest;
import com.itset.itcenteamproject.domain.user.dto.SurveyCheckResponseDTO;
import com.itset.itcenteamproject.domain.user.service.SessionUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/surveys")
@Tag(name = "serveys", description = "설문 관련 api")
public class SurveyController {
    private final SurveyService surveyService;
    private final SessionUserService sessionUserService;

    @PostMapping
    @Operation(summary = "설문 등록")
    public ResponseEntity<Long> createSurvey(@RequestBody @Valid SurveyCreateRequest request,
                                             @SessionAttribute("loginUser") Long userId) {
        Long surveyId = surveyService.createSurvey(request,userId);//생성된 질문 ID
        return ResponseEntity.status(HttpStatus.CREATED).body(surveyId);//200 대신 201응답
    }

    //의도하지 않은 경로로 대시보드 진입 방지
    //프론트에서 대시보드 페이지 진입 전 호출
    @Operation(description = "의도하지 않은 경로로 대시보드 진입 방지," +
            " 프론트에서 대시보드 페이지 진입 전 호출", summary = "세션 유저의 설문 유무 조회" )
    @GetMapping("/auth/survey-check")
    public SurveyCheckResponseDTO checkSurvey(HttpSession session) {
        Long userId = sessionUserService.getLoginUserId(session);
        boolean surveyCompleted = surveyService.hasSurvey(userId);
        String redirectPath = surveyCompleted ? "/dashboard" : "/surveys";

        return new SurveyCheckResponseDTO(surveyCompleted, redirectPath);
    }
}
