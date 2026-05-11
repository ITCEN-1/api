package com.itset.itcenteamproject.domain.dashboard.controller;

import com.itset.itcenteamproject.domain.dashboard.dto.DongDetailResponse;
import com.itset.itcenteamproject.domain.dashboard.dto.InfraDetailResponse;
import com.itset.itcenteamproject.domain.dashboard.dto.InfraType;
import com.itset.itcenteamproject.domain.dashboard.DashboardService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
            HttpSession session
    ) {
        Long userId = (Long) session.getAttribute("loginUser");
        return dashboardService.getDongSummary(userId, surveyId, dongCode);
    }

    // 인프라 요소별 상세 조회
    @GetMapping("/dongs/{dongCode}/elements")
    public InfraDetailResponse getInfraDetails(
            @PathVariable Integer dongCode,
            @RequestParam Long surveyId,
            @RequestParam InfraType type,
            HttpSession session
    ) {
        Long userId = (Long) session.getAttribute("loginUser");
        return dashboardService.getInfraDetails(userId, surveyId, dongCode, type);
    }
}