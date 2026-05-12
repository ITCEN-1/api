package com.itset.itcenteamproject.domain.dashboard;

import com.itset.itcenteamproject.domain.dashboard.dto.DongDetailResponse;
import com.itset.itcenteamproject.domain.dashboard.dto.InfraDetailResponse;
import com.itset.itcenteamproject.domain.dashboard.dto.InfraType;
import com.itset.itcenteamproject.domain.dashboard.DashboardService;

import jakarta.servlet.http.HttpSession;
import com.itset.itcenteamproject.domain.dashboard.model.RecommendedDong;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    // 동 상세 요약 조회
    @GetMapping("/dongs/{dongCode}")
    public DongDetailResponse getDongSummary(
            @PathVariable Integer dongCode,
            @RequestParam Long surveyId,
            @SessionAttribute("loginUser") Long userId
    ) {
        return dashboardService.getDongSummary(userId, surveyId, dongCode);
    }

    // 인프라 요소별 상세 조회
    @GetMapping("/dongs/{dongCode}/elements")
    public InfraDetailResponse getInfraDetails(
            @PathVariable Integer dongCode,
            @RequestParam Long surveyId,
            @RequestParam InfraType type,
            @SessionAttribute("loginUser") Long userId
    ) {
        return dashboardService.getInfraDetails(userId, surveyId, dongCode, type);
    }
    // 점수를 가져오면서 히스토리에 저장도 하는거라 GET 애매하긴함
    @Operation(summary = "동 랭킹 조회 API" ,description = "현재 세션에 유저의 가장 최근 설문으로 동 랭킹을 조회하고 히스토리에 저장합니다")
    @GetMapping
    public List<RecommendedDong> getRanking(@SessionAttribute("loginUser") Long userId){
       return dashboardService.getRanking(userId);
    }

    @Operation(summary = "동 랭킹 조회 API 테스트" ,description = "테스트용, 파라미터로 직접 UserId 값을 입력받습니다")
    @GetMapping("/test")
    public List<RecommendedDong> getRankingTest(@RequestParam Long userId){
        return dashboardService.getRanking(userId);
    }
}
