package com.itset.itcenteamproject.domain.dashboard;

import com.itset.itcenteamproject.domain.dashboard.dto.DongDetailResponse;
import com.itset.itcenteamproject.domain.dashboard.dto.InfraDetailResponse;
import com.itset.itcenteamproject.domain.dashboard.dto.InfraType;
import com.itset.itcenteamproject.domain.dashboard.DashboardService;

import jakarta.servlet.http.HttpSession;
import com.itset.itcenteamproject.domain.dashboard.model.RecommendedDong;
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
    @GetMapping
    public List<RecommendedDong> getRanking(@SessionAttribute("loginUser") Long userId){
       return dashboardService.getRanking(userId);
    }
}