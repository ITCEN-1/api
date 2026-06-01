package com.itset.itcenteamproject.domain.dashboard.controller;

import com.itset.itcenteamproject.domain.dashboard.dto.DongDetailResponse;
import com.itset.itcenteamproject.domain.dashboard.dto.InfraDetailResponse;
import com.itset.itcenteamproject.domain.dashboard.dto.InfraType;
import com.itset.itcenteamproject.domain.dashboard.service.DashboardService;

import com.itset.itcenteamproject.domain.dashboard.model.RecommendedDong;
import com.itset.itcenteamproject.domain.user.service.SessionUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
@Tag(name = "dashboard", description = "대시보드 관련 api")
public class DashboardController {

    private final DashboardService dashboardService;
    private final SessionUserService sessionUserService;

    // 동 상세 요약 조회
    @GetMapping("/dongs/{dongCode}")
    @Operation(summary = "동 상세 조회", description = "동 상세 정보를 가져옵니다, 세션 유저의 설문,히스토리 기반으로 필터링된 상세 정보입니다")
    public DongDetailResponse getDongSummary(
            @PathVariable Integer dongCode,
            @RequestParam Long surveyId
    ) {
        Long userId = sessionUserService.getLoginUserId();

        return dashboardService.getDongSummary(userId, surveyId, dongCode);
    }

    // 인프라 요소별 상세 조회
    @GetMapping("/dongs/{dongCode}/elements")
    @Operation(summary = "인프라 요소별 상세 조회",description = "InfraType 에 해당하는 인프라 요소를 조회합니다")
    public InfraDetailResponse getInfraDetails(
            @PathVariable Integer dongCode,
            @RequestParam InfraType type
    ) {
        return dashboardService.getInfraDetails(dongCode, type);
    }

    // 점수를 가져오면서 히스토리에 저장도 하는거라 GET 애매하긴함
    @Operation(summary = "동 랭킹 조회" ,description = "현재 세션에 유저의 가장 최근 설문으로 동 랭킹을 조회하고 히스토리에 저장합니다")
    @GetMapping
    public List<RecommendedDong> getRanking(){
        Long userId = sessionUserService.getLoginUserId();

        return dashboardService.getRanking(userId);
    }

    @Operation(summary = "[Test] 동 랭킹 조회" ,description = "테스트용, 파라미터로 직접 UserId 값을 입력받습니다")
    @GetMapping("/test")
    public List<RecommendedDong> getRankingTest(@RequestParam Long userId){
        return dashboardService.getRanking(userId);
    }
}
