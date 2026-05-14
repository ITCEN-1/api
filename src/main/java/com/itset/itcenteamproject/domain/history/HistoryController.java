package com.itset.itcenteamproject.domain.history;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "hisotry",description = "히스토리 관련 api")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    // Pageable 어노테이션에서는 기본 PageSize: 10, pageNum: 0입니다.
    @Operation(summary = "세션 유저의 히스토리 조회")
    @GetMapping("/history")
    public List<HistoryDTO> getHistory(@SessionAttribute("loginUser") Long userId,
                                       @PageableDefault Pageable pageable) {
        return historyService.getHistory(userId, pageable);
    }
}
