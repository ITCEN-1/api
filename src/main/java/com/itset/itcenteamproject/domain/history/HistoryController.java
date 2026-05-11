package com.itset.itcenteamproject.domain.history;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HistoryController {

    @GetMapping("/history")
    public String getHistory() {

        return "history";
    }

}
