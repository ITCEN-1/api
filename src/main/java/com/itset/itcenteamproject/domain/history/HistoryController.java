package com.itset.itcenteamproject.domain.history;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

@RestController
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    // Pageable 어노테이션에서는 기본 PageSize: 10, pageNum: 0입니다.
    @GetMapping("/history")
    public List<HistoryDTO> getHistory(@SessionAttribute("loginUser") String userId,
                                       @PageableDefault Pageable pageable) {
        return historyService.getHistory(userId, pageable);
    }
}
