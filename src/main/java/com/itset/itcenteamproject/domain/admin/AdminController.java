package com.itset.itcenteamproject.domain.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/admin")
    public String adminPage(Model model){
        //헤더 기간 (ex "2026년 5월")
        String period = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 M월"));
        model.addAttribute("period", period);

        //상단의 누적 유저/설문/게시글
        model.addAttribute("kpis", adminService.getKpis());

        //가장 많이 추천된 상위 10개 동 + 막대 정규화용 최댓값
        List<AdminService.TopDongRow> adminDongs = adminService.getTop10DongCode();
        long maxDongCount = adminDongs.stream()
                .mapToLong(AdminService.TopDongRow::count)
                .max()
                .orElse(1L);
        model.addAttribute("adminDongs", adminDongs);
        model.addAttribute("maxDongCount", maxDongCount);

        //유저 추이 (최근 10일 일별 가입자)
        model.addAttribute("userTrend", adminService.getRecentDailySignups());

        //설문 평균 (인프라 선호도)
        model.addAttribute("surveyRows", adminService.getSurveyResult());

        return "admin"; //templates/admin.html 으로 이동
    }
}
