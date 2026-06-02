package com.itset.itcenteamproject.domain.survey;

import com.itset.itcenteamproject.domain.survey.dto.SurveyCreateRequest;
import com.itset.itcenteamproject.domain.survey.dto.SurveyDTO;
import com.itset.itcenteamproject.domain.user.dto.SurveyCheckResponseDTO;
import com.itset.itcenteamproject.global.component.SessionManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/surveys")
@Tag(name = "serveys", description = "설문 관련 api")
public class SurveyController {
    private final SurveyService surveyService;
    private final SessionManager sessionManager;

    @PostMapping
    @Operation(summary = "설문 등록",description = "설문 작성자는 현재 세션 유저로 등록됩니다")
    public SurveyDTO createSurvey(@RequestBody @Valid SurveyCreateRequest request) {
        return surveyService.createSurvey(request);//설문 전체 응답
    }

    //의도하지 않은 경로로 대시보드 진입 방지
    //프론트에서 대시보드 페이지 진입 전 호출
    @Operation(description = "의도하지 않은 경로로 대시보드 진입 방지," +
            " 프론트에서 대시보드 페이지 진입 전 호출, 현재 세션 유저의 설문 유무를 조회합니다", summary = "설문 유무 조회" )
    @GetMapping("/status")
    public SurveyCheckResponseDTO checkSurvey() {
        Long userId = sessionManager.getLoginUserId();
        boolean surveyCompleted = surveyService.hasSurvey(userId);
        String redirectPath = surveyCompleted ? "/dashboard" : "/surveys";

        return new SurveyCheckResponseDTO(surveyCompleted, redirectPath);
    }
}
