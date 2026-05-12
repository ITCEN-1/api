package com.itset.itcenteamproject.domain.history;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class HistoryItemDTO {
    private Integer ranking;
    private Integer dongCode;
    private Integer commuteTime;

    public static HistoryItemDTO from(HistoryItem historyItem) {
        return HistoryItemDTO.builder()
                .ranking(historyItem.getRanking())
                .dongCode(historyItem.getDongCode())
                .commuteTime(historyItem.getCommuteTime())
                .build();
    }
}
