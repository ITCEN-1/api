package com.itset.itcenteamproject.domain.history;

import com.itset.itcenteamproject.domain.user.service.SessionUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "history",description = "히스토리 관련 api")
public class HistoryController {

    private final HistoryService historyService;
    private final SessionUserService sessionUserService;

    public HistoryController(HistoryService historyService, SessionUserService sessionUserService) {
        this.historyService = historyService;
        this.sessionUserService = sessionUserService;
    }

    @Operation(summary = "히스토리 목록 내 특정 히스토리 조회")
    @GetMapping("/history/{surveyId}")
    public HistoryDTO getSurveyAndHistory(@PathVariable Long surveyId) {
        return historyService.getHistory(surveyId);
    }
      
    @Operation(summary = "히스토리 조회",description = "세션 유저의 히스토리 조회")
    @GetMapping("/history")
    public List<HistoryDTO> getHistory(@PageableDefault Pageable pageable) {
        Long userId = sessionUserService.getLoginUserId();

        return historyService.getHistory(userId, pageable);
    }
}
