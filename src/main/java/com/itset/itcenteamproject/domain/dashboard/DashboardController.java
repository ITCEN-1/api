package com.itset.itcenteamproject.domain.dashboard;

import com.itset.itcenteamproject.domain.dashboard.model.RecommendedDong;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboards")
public class DashboardController {

    private final DashboardService dashboardService;

    // 점수를 가져오면서 히스토리에 저장도 하는거라 GET 애매하긴함
    @GetMapping
    public List<RecommendedDong> getRanking(@SessionAttribute("loginUser") Long userId){
       return dashboardService.getRanking(userId);
    }
}
